package com.maple.rabbitMQ.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenqf
 */
@Configuration
public class AmqpConfig {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue demoQueue() {
        Queue queue = new Queue("mirror_demo");
        amqpAdmin.declareQueue(queue);
        return queue;
    }
    @Bean
    public Queue demoQueue2() {
        Queue queue = new Queue("mirror_demo2");
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    @Bean
    public DirectExchange demoExchange() {
        DirectExchange exchange = new DirectExchange("demo");
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(demoQueue()).to(demoExchange()).with("demoKey");
    }

    @Bean
    public Binding binding2() {
        return BindingBuilder.bind(demoQueue2()).to(demoExchange()).with("demoKey");
    }
}
