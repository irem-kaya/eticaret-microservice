package com.eticaret.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "product-events";

    // Queue isimleri
    public static final String ORDER_CREATED_QUEUE    = "notification.order.created";
    public static final String PAYMENT_COMPLETED_QUEUE = "notification.payment.completed";
    public static final String PAYMENT_FAILED_QUEUE   = "notification.payment.failed";
    public static final String LOW_STOCK_QUEUE        = "notification.low.stock";

    @Bean
    public TopicExchange productEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE).build();
    }

    @Bean public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE).build();
    }

    @Bean public Queue lowStockQueue() {
        return QueueBuilder.durable(LOW_STOCK_QUEUE).build();
    }

    @Bean public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(productEventsExchange()).with("order.created");
    }

    @Bean public Binding paymentCompletedBinding() {
        return BindingBuilder.bind(paymentCompletedQueue())
                .to(productEventsExchange()).with("payment.completed");
    }

    @Bean public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue())
                .to(productEventsExchange()).with("payment.failed");
    }

    @Bean public Binding lowStockBinding() {
        return BindingBuilder.bind(lowStockQueue())
                .to(productEventsExchange()).with("product.low-stock");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setDefaultRequeueRejected(false); // hatalı mesajı tekrar kuyruğa atma
        return factory;
    }
}