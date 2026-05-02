package com.eticaret.user.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.user}")
    private String userQueue;

    @Value("${app.rabbitmq.queue.log}")
    private String logQueue;

    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredKey;

    @Value("${app.rabbitmq.routing-key.log}")
    private String logKey;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue userQueue() {
        return QueueBuilder.durable(userQueue).build();
    }

    @Bean
    public Queue logQueue() {
        return QueueBuilder.durable(logQueue).build();
    }

    @Bean
    public Binding userBinding() {
        return BindingBuilder.bind(userQueue()).to(exchange()).with(userRegisteredKey);
    }

    @Bean
    public Binding logBinding() {
        return BindingBuilder.bind(logQueue()).to(exchange()).with(logKey);
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
}