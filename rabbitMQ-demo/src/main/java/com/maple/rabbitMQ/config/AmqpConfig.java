package com.maple.rabbitMQ.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.HashMap;

/**
 * 用于自动创建 exchange queue binding
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
    public Queue queue_demo() {
        Queue queue = new Queue("queue-demo1",true,false,false);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    public Queue singleActiveQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-single-active-consumer",true); // 单活模式
        map.put("x-queue-type","quorum"); // 指定为 quorum 类型的队列
        Queue queue = new Queue("single-active-demo",true,false,false,map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    /**
     * 声明 classic 队列
     */
    public Queue classicQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange","dead-letter-exchange"); // 指定死信队列的exchange
        map.put("x-dead-letter-routing-key","dead-routing-key"); // 指定死信队列的exchange
        Queue queue = new Queue("classic-demo",true,false,false,map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    /**
     * 声明延时队列 ,声明过期时间, 声明对应的死信队列
     */
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
    public Queue quorumQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-queue-type","quorum"); // 指定为 quorum 类型的队列
        map.put("x-delivery-limit",10); // 指定消息重试次数,总次数为n+1, 超过次数进入死信队列
        map.put("x-dead-letter-exchange","dead-letter-exchange"); // 指定死信队列的exchange
        map.put("x-dead-letter-routing-key","dead-routing-key"); // 指定死信队列的exchange
        Queue queue = new Queue("quorum-demo",true,false,false,map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    /**
     * 声明用作死信的 quorum 队列
     */
    public Queue deadLetterQuorumQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-queue-type","quorum"); // 指定为 quorum 类型的队列
        Queue queue = new Queue("dead-letter-quorum-demo",true,false,false,map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }
    public DirectExchange demo_exchange() {
        DirectExchange exchange = new DirectExchange("exchange-demo1",true,false);
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }

    public DirectExchange demoExchange() {
        DirectExchange exchange = new DirectExchange("exchange-demo",true,false);
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }
    public DirectExchange demoExchange1() {
        DirectExchange exchange = new DirectExchange("classic-exchange-demo",true,false);
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }

    /**
     * 声明用作死信的 exchange
     */
    public DirectExchange deadLetterExchange() {
        DirectExchange exchange = new DirectExchange("dead-letter-exchange",true,false);
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }

//    @Bean
//    public DirectExchange shardingExchange() {
//        Exchange exchange = new Exchange();
//        amqpAdmin.declareExchange(exchange);
//        return exchange;
//    }

    @Bean
    public Queue tempQueue(){
        // 创建一个临时队列
        Queue queue = new AnonymousQueue();
        FanoutExchange exchange = new FanoutExchange("process-cache-exchange");
        Binding binding = BindingBuilder.bind(queue).to(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareBinding(binding);
        return queue;
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
    public Binding binding6() {
        return BindingBuilder.bind(singleActiveQueue()).to(demoExchange()).with("demo");
    }

    @Bean
    public Binding binding4() {
        return BindingBuilder.bind(deadLetterQuorumQueue()).to(deadLetterExchange()).with("dead-routing-key");
    }
    @Bean
    public Binding binding5() {
        return BindingBuilder.bind(classicQueue()).to(demoExchange1()).with("demo");
    }

    @Bean
    public Binding binding7() {
        return BindingBuilder.bind(queue_demo()).to(demo_exchange()).with("demo");
    }

    @Bean
    public Binding binding8() {
        Queue queue = new Queue("batch-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        DirectExchange exchange = new DirectExchange("batch-exchange",true,false);
        amqpAdmin.declareExchange(exchange);
        return BindingBuilder.bind(queue).to(exchange).with("demo");
    }

}
