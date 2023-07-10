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
@RequestMapping("/receive")
public class MqReceiveController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/China")
    public Result China(){
        Object o = rabbitTemplate.receiveAndConvert("China");
        System.out.println(o.getClass());
        System.out.println(o.toString());
        return Result.success(o.toString());
    }

    @RequestMapping("/China.news")
    public Result C_news(){
        Object o = rabbitTemplate.receiveAndConvert("China.news");
        System.out.println(o.getClass());
        System.out.println(o.toString());
        return Result.success(o.toString());
    }

    @RequestMapping("/US.news")
    public Result U_news(){
        Object o = rabbitTemplate.receiveAndConvert("US.news");
        System.out.println(o.getClass());
        System.out.println(o.toString());
        return Result.success(o.toString());
    }
}
