package com.maple.rabbitMQ.service;

import com.maple.rabbitMQ.pojo.Book;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author chenqf
 */
@Service
public class BookService {
    @RabbitListener(queues = "China")
    public void receive(Book book){ // 监听消息
        System.out.println("收到消息:" + book);
    }

    @RabbitListener(queues = "China.news")
    public void receive(Message message){ // 监听头信息
        System.out.println(message.getBody().toString());
        System.out.println(message.getMessageProperties().toString());
    }
}
