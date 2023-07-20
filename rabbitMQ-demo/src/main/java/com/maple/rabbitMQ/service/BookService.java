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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chenqf
 */
@Service
public class BookService {

    @Autowired
    private ObjectMapper objectMapper;

//    @RabbitListener(queues = "demo1")
//    public void receive(Book book){ // 监听消息
//        System.out.println("收到消息:" + book);
//    }

    @Value("${server.port}")
    Integer port;


    /**
     * 验证接到消息, 但不应答,消息是否能从unacked变为Ready
     * 现已经 unacked
     */
    @RabbitListener(queues = "classic-demo",ackMode = "AUTO")
    public void receiveAck1(Book book,Message message,Channel channel) throws IOException, InterruptedException {
//        System.out.println("classic-demo - 收到消息:" + book);
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // 应答, 消息消费成功
//        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,true); // 拒绝, 并重新入队
    }

    @RabbitListener(queues = "quorum-demo")
    public void receiveAck2(Book book,Message message,Channel channel) throws IOException, InterruptedException {
        // 当前第几次接收到该消息 deliveryCount 从0开始
        Object o = message.getMessageProperties().getHeaders().get("x-delivery-count");
        long deliveryCount = o == null ? 0 : (long) o;
        System.out.println("第" + deliveryCount + "次收到消息:" + book);
        if(deliveryCount >= 4){
            System.out.println("手动丢弃");
            // 拒绝, 丢弃消息进入死信队列
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);
            // 应答, 消息消费成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }else{
            System.out.println("重新入队");
            // 拒绝, 并重新入队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,true);
        }
    }

//    @RabbitListener(queues = "dead-letter-quorum-demo")
//    public void receiveAck3(Book book,Message message,Channel channel) throws IOException, InterruptedException {
//        // 当前第几次接收到该消息 deliveryCount 从0开始
//        Object o = message.getMessageProperties().getHeaders().get("x-delivery-count");
//        long deliveryCount = o == null ? 0 : (long) o;
//        System.out.println("第" + deliveryCount + "次收到消息:" + book);
//        if(deliveryCount >= 4){
//            // 拒绝, 丢弃消息进入死信队列
////            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);
//            // 应答, 消息消费成功
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//        }else{
//            // 拒绝, 并重新入队
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,true);
//        }
//    }

//    @RabbitListener(queues = "demo2")
//    public void receive(Message message){
//        System.out.println(message.getBody().toString());
//    }
}
