package com.maple.rabbitMQ.controller;

import com.maple.rabbitMQ.pojo.Book;
import com.maple.rabbitMQ.utils.MessageSender;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author chenqf
 */
@RestController
@RequestMapping("/send")
public class MqSentController {

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/demo")
    public String direct() {
        for (int i = 0; i < 5; i++) {
            this.messageSender.rabbitConvertAndSend("exchange-demo", "demo", new Book(1, "chenqf", "haha"));
        }
        return "1111";
    }

    @RequestMapping("/demo1")
    public String direct1() {
        this.messageSender.rabbitConvertAndSend("exchange-demo", "demo2222", new Book(1, "chenqf", "haha"));
        return "222";
    }

    @RequestMapping("/demo2")
    public String direct2() {
        Book book = new Book(1, "chenqf", "haha");
        this.messageSender.rabbitConvertAndSend("exchange-demo1111", "demo2222", book);
        return "222333";
    }

    @RequestMapping("/demo3")
    public String direct3() {
        for (int i = 1; i <= 10000; i++) {
            this.rabbitTemplate.convertAndSend("classic-exchange-demo","demo","test" +i,new CorrelationData("test_" + i));
        }
        return "batch create";
    }

    @RequestMapping("/demo4")
    public String direct4() {
        this.rabbitTemplate.convertAndSend("classic-exchange-demo","demo","test",new CorrelationData("test_"));
        return "batch create";
    }

    @RequestMapping("/demo5")
    public String direct5() throws InterruptedException {
        for (int i = 1; i <= 100; i++) {
            this.messageSender.rabbitConvertAndSend("exchange-demo", "demo", new Book(i, "chenqf_" + i, "haha:" + i));
            Thread.sleep(200);
        }
        return "demo5";
    }
}
