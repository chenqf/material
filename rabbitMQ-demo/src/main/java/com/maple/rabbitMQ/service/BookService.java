package com.maple.rabbitMQ.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maple.rabbitMQ.pojo.Book;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chenqf
 */
@Service
public class BookService {

    @RabbitListener(queues = "classic-demo",concurrency = "1",containerFactory = "consumerBatchContainerFactory")
    public void receive(List<Book> books, List<Message> messages, Channel channel) throws Exception {
        System.out.println(books.size() + ":thread-id: " + Thread.currentThread().getId());
        for (Book book : books) {
            System.out.println(book);
        }
        System.out.print(" --------------------");
    }

    @RabbitListener(queues = "batch-queue", concurrency = "1")
    public void receive1(Book book, Message message, Channel channel) throws Exception {
//
        System.out.println(book.getAuthor() + " --------------------" + book);
    }

    @RabbitListener(queues = "queue-demo1", concurrency = "3")
    public void receive(Book book, Channel channel) throws Exception {
        System.out.println("---------------------------");
        System.out.println("消费:" + book + "-thread:" + Thread.currentThread().getId());
        System.out.println("---------------------------");
    }

    @RabbitListener(queues = "single-active-demo", concurrency = "3")
    public void receiveSingle(Book book, Channel channel) throws Exception {
        System.out.println(book + "single-active-demo-thread:" + Thread.currentThread().getId());
        Thread.sleep(1000);
    }


    //    @RabbitListener(queues = "classic-demo",ackMode = "AUTO")
    public void receiveAck1(String msg, Message message, Channel channel) throws Exception {

        throw new Exception("一个异常");
//        System.out.println("classic-demo - 收到消息:" + book);
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // 应答, 消息消费成功
//        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,true); // 拒绝, 并重新入队
    }

    @RabbitListener(queues = "quorum-demo", ackMode = "MANUAL")
    @RabbitHandler
    public void receiveAck2(Book book, Message message, Channel channel) throws IOException, InterruptedException {
        // 当前第几次接收到该消息 deliveryCount 从0开始
        Object o = message.getMessageProperties().getHeaders().get("x-delivery-count");
        long deliveryCount = o == null ? 0 : (long) o;
        if (deliveryCount >= 5) {
            // 拒绝, 丢弃消息进入死信队列
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);
            // 应答, 消息消费成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            // 拒绝, 并重新入队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
