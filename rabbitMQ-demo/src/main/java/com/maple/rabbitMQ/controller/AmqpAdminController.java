package com.maple.rabbitMQ.controller;

import com.maple.common.domain.Result;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqf
 */
@RestController
@RequestMapping("/amqp")
public class AmqpAdminController {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @RequestMapping("/create")
    public Result create(){

        // 创建 exchange
        amqpAdmin.declareExchange(new DirectExchange("exchanges.direct1"));
        amqpAdmin.declareExchange(new FanoutExchange("exchanges.fanout1"));
        amqpAdmin.declareExchange(new TopicExchange("exchanges.topic1"));

        // 创建 Queue
        amqpAdmin.declareQueue(new Queue("amqp.queue",true));

        // 创建 Binding
        amqpAdmin.declareBinding(new Binding("amqp.queue", Binding.DestinationType.QUEUE,"exchanges.direct1","routingKey-a",null));

        return Result.success("");
    }

    @RequestMapping("/delete")
    public Result delete(){

        // 删除 exchange
        amqpAdmin.deleteExchange("exchanges.direct1");
        amqpAdmin.deleteExchange("exchanges.fanout1");
        amqpAdmin.deleteExchange("exchanges.topic1");

        // 删除 queue
        amqpAdmin.deleteQueue("amqp.queue");

        return Result.success("");
    }
}
