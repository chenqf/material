package com.maple.rabbitMQ.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

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

    /**
     * 声明 classic 队列
     */
    @Bean
    public Queue classicQueue() {
        Queue queue = new Queue("classic-demo",true,false,false);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    /**
     * 声明延时队列 ,声明过期时间, 声明对应的死信队列
     */
    @Bean
    public Queue delayQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange","dead-letter-exchange"); // 指定死信队列的exchange
        map.put("x-dead-letter-routing-key","dead-routing-key"); // 指定死信队列的exchange
        map.put("x-message-ttl",3000); // 过期时间, 单位ms, 超过时间进入死信队列
        Queue queue = new Queue("daily-demo",true,false,false,map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    /**
     * 声明 quorum 队列并指定对应的死信队列
     */
    @Bean
    public Queue quorumQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-queue-type","quorum"); // 指定为 quorum 类型的队列
        map.put("x-delivery-limit",3); // 指定消息重试次数,总次数为n+1, 超过次数进入死信队列
        map.put("x-dead-letter-exchange","dead-letter-exchange"); // 指定死信队列的exchange
        map.put("x-dead-letter-routing-key","dead-routing-key"); // 指定死信队列的exchange
        Queue queue = new Queue("quorum-demo",true,false,false,map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    /**
     * 声明用作死信的 quorum 队列
     */
    @Bean
    public Queue deadLetterQuorumQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-queue-type","quorum"); // 指定为 quorum 类型的队列
        Queue queue = new Queue("dead-letter-quorum-demo",true,false,false,map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    @Bean
    public DirectExchange demoExchange() {
        DirectExchange exchange = new DirectExchange("exchange-demo",true,false);
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }

    /**
     * 声明用作死信的 exchange
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        DirectExchange exchange = new DirectExchange("dead-letter-exchange",true,false);
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }


    @Bean
    public Binding binding1() {
        return BindingBuilder.bind(quorumQueue()).to(demoExchange()).with("demo");
    }

    @Bean
    public Binding binding2() {
        return BindingBuilder.bind(classicQueue()).to(demoExchange()).with("demo");
    }

    @Bean
    public Binding binding3() {
        return BindingBuilder.bind(delayQueue()).to(demoExchange()).with("demo");
    }

    @Bean
    public Binding binding4() {
        return BindingBuilder.bind(deadLetterQuorumQueue()).to(deadLetterExchange()).with("dead-routing-key");
    }

}
