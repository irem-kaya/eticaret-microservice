package com.eticaret.order.domain;

import com.eticaret.order.dto.*;
import com.eticaret.order.event.OrderCreatedEvent;
import com.eticaret.order.exception.OrderNotFoundException;
import com.eticaret.order.facade.OrderFacade;
import com.eticaret.order.validation.OrderValidationChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderFacade Unit Tests")
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderValidationChain validationChain;
    @Mock RabbitTemplate rabbitTemplate;

    @InjectMocks OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderFacade, "exchange", "test-exchange");
        ReflectionTestUtils.setField(orderFacade, "orderCreatedKey", "order.created");
    }

    private OrderItemRequest buildItemRequest() {
        return new OrderItemRequest(1L, "Laptop", new BigDecimal("15000"), 2);
    }

    private CreateOrderRequest buildOrderRequest() {
        return new CreateOrderRequest(
                List.of(buildItemRequest()),
                new BigDecimal("30000"),
                "İstanbul, Türkiye"
        );
    }

    private Order buildOrder(Long id, String userId) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setTotalAmount(new BigDecimal("30000"));
        order.setShippingAddress("İstanbul, Türkiye");
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("geçerli request → sipariş kaydedilir ve event yayınlanır")
        void createOrder_validRequest_savesAndPublishesEvent() {
            CreateOrderRequest req = buildOrderRequest();
            Order saved = buildOrder(1L, "user-123");

            when(orderRepository.save(any())).thenReturn(saved);

            OrderResponse resp = orderFacade.createOrder("user-123", req);

            assertThat(resp).isNotNull();
            verify(validationChain).validate(req);
            verify(orderRepository).save(any(Order.class));
            verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("order.created"), any(OrderCreatedEvent.class));
        }

        @Test
        @DisplayName("validasyon hatası → exception fırlatılır, kayıt yapılmaz")
        void createOrder_validationFails_throwsAndNoSave() {
            CreateOrderRequest req = buildOrderRequest();
            doThrow(new com.eticaret.order.exception.OrderValidationException("Yetersiz stok"))
                    .when(validationChain).validate(req);

            assertThatThrownBy(() -> orderFacade.createOrder("user-123", req))
                    .isInstanceOf(com.eticaret.order.exception.OrderValidationException.class);

            verify(orderRepository, never()).save(any());
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }

        @Test
        @DisplayName("RabbitMQ hatası → exception fırlatılmaz")
        void createOrder_rabbitFails_noExceptionPropagated() {
            CreateOrderRequest req = buildOrderRequest();
            Order saved = buildOrder(1L, "user-123");

            when(orderRepository.save(any())).thenReturn(saved);
            doThrow(new RuntimeException("rabbit down"))
                    .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(OrderCreatedEvent.class));

            assertThatNoException().isThrownBy(() -> orderFacade.createOrder("user-123", req));
        }
    }

    @Nested
    @DisplayName("getOrderById()")
    class GetOrderById {

        @Test
        @DisplayName("mevcut id → OrderResponse döner")
        void getOrderById_exists_returnsResponse() {
            Order order = buildOrder(1L, "user-123");
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            OrderResponse resp = orderFacade.getOrderById(1L);

            assertThat(resp).isNotNull();
        }

        @Test
        @DisplayName("bilinmeyen id → OrderNotFoundException")
        void getOrderById_notFound_throws() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderFacade.getOrderById(99L))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getUserOrders()")
    class GetUserOrders {

        @Test
        @DisplayName("kullanıcının siparişleri → page döner")
        void getUserOrders_returnsPage() {
            Order order = buildOrder(1L, "user-123");
            Page<Order> page = new PageImpl<>(List.of(order));
            when(orderRepository.findByUserId(eq("user-123"), any())).thenReturn(page);

            Page<OrderResponse> result = orderFacade.getUserOrders("user-123", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("siparişi olmayan kullanıcı → boş page")
        void getUserOrders_noOrders_emptyPage() {
            when(orderRepository.findByUserId(eq("user-999"), any()))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<OrderResponse> result = orderFacade.getUserOrders("user-999", PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("geçerli id → durum güncellenir")
        void updateStatus_validId_updatesStatus() {
            Order order = buildOrder(1L, "user-123");
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);

            OrderResponse resp = orderFacade.updateStatus(1L, OrderStatus.CONFIRMED);

            assertThat(resp).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("bilinmeyen id → OrderNotFoundException")
        void updateStatus_notFound_throws() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderFacade.updateStatus(99L, OrderStatus.CONFIRMED))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }
}