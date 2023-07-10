package com.maple.rabbitMQ.controller;

import com.maple.common.domain.Result;
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

    @RequestMapping("/direct1")
    public Result direct1(){
        rabbitTemplate.convertAndSend("exchanges.direct","China.people","来自springBoot的message");
        return Result.success("");
    }

    @RequestMapping("/direct2")
    public Result direct2(){
        Book book = new Book(1,"好好学习","chenqf");
        rabbitTemplate.convertAndSend("exchanges.direct","China",book);
        return Result.success("");
    }

    @RequestMapping("/direct3")
    public Result direct3(){
        Book book = new Book(1,"好好学习","chenqf");
        rabbitTemplate.convertAndSend("exchanges.direct","China.news",book);
        return Result.success("");
    }

    @RequestMapping("/fanout1")
    public Result fanout1(){
        rabbitTemplate.convertAndSend("exchanges.fanout","","来自springBoot的 fanout message");
        return Result.success("");
    }

    @RequestMapping("/topic1")
    public Result topic1(){
        rabbitTemplate.convertAndSend("exchanges.topic","haha.news","来自springBoot的 topic message");
        return Result.success("");
    }
}
