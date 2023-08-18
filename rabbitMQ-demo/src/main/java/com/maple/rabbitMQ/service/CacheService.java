package com.maple.rabbitMQ.service;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author 陈其丰
 */
@Service
public class CacheService {

//    @Autowired
//    @Qualifier("updateCacheQueue")
//    private Queue queue;

    @RabbitListener(queues = "#{tempQueue.name}")
    public void listenToTemporaryQueue(String message) {
//        System.out.println(queue);
        // 处理接收到的消息
        System.out.println("Received message from temporary queue: " + message);
    }
}
