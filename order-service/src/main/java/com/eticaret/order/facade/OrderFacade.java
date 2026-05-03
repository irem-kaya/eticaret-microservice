package com.eticaret.order.facade;

import com.eticaret.order.domain.*;
import com.eticaret.order.dto.*;
import com.eticaret.order.event.OrderCreatedEvent;
import com.eticaret.order.exception.OrderNotFoundException;
import com.eticaret.order.validation.OrderValidationChain;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderFacade {

    private final OrderRepository orderRepository;
    private final OrderValidationChain validationChain;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.order-created}")
    private String orderCreatedKey;

    public OrderFacade(OrderRepository orderRepository,
                       OrderValidationChain validationChain,
                       RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.validationChain = validationChain;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        // 1. Chain of Responsibility ile doÄŸrula
        validationChain.validate(request);

        // 2. SipariÅŸi oluÅŸtur
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(request.totalAmount());
        order.setShippingAddress(request.shippingAddress());

        // 3. Order item'larÄ± ekle
        request.items().forEach(itemReq -> {
            OrderItem item = new OrderItem();
            item.setProductId(itemReq.productId());
            item.setProductName(itemReq.productName());
            item.setPrice(itemReq.price());
            item.setQuantity(itemReq.quantity());
            item.setOrder(order);
            order.getItems().add(item);
        });

        Order saved = orderRepository.save(order);

        // 4. RabbitMQ'ya event gÃ¶nder
        try {
            rabbitTemplate.convertAndSend(exchange, orderCreatedKey,
                    OrderCreatedEvent.of(saved));
        } catch (Exception e) {
            System.out.println("RabbitMQ event gonderilemedi: " + e.getMessage());
        }
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return OrderResponse.from(orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id)));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.setStatus(status);
        return OrderResponse.from(orderRepository.save(order));
    }
}
