package com.maple.rabbitMQ.controller;

import com.maple.rabbitMQ.pojo.Book;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqf
 */
@RestController
@RequestMapping("/send")
public class MqSentController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/demo")
    public String direct1(){
        rabbitTemplate.convertAndSend("demo","demoKey",new Book(1,"chenqf","haha"));
        return "";
    }
}
