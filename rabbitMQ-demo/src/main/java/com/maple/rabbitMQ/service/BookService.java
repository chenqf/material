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
    @RabbitListener(queues = "mirror_demo")
    public void receive(Book book){ // 监听消息
        System.out.println("收到消息:" + book);
    }

    @RabbitListener(queues = "mirror_demo2")
    public void receive(Message message){
        System.out.println(message.getBody().toString());
    }
}
