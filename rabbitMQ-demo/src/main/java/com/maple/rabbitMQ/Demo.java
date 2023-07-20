package com.maple.rabbitMQ;

import com.rabbitmq.client.*;
import lombok.Cleanup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author 陈其丰
 */
public class Demo {
    private static String host = "121.36.70.23";
    private static String port = "5672";
    private static String username = "admin";
    private static String password = "chenqf";

    private static String virtualHost = "/";

    public static void  createStreamQueue(Channel channel,String queueName) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-queue-type","stream");
        map.put("x-max-length-bytes",20_000_000_000L);
        map.put("x-stream-max-segment-size-bytes",100_000_000L);
        channel.queueDeclare(queueName,true,false,false,map);
    }
    public static void  createQuorumQueue(Channel channel,String queueName) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-queue-type","quorum");
        channel.queueDeclare(queueName,true,false,false,map);
    }
    public static void createClassicQueue(Channel channel,String queueName) throws IOException {
        channel.queueDeclare(queueName,true,false,false,null);
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(Integer.parseInt(port));
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        Connection connection = factory.newConnection();
        // 可创建多个 channel
        Channel channel = connection.createChannel();

        // 客户端每次能拿多少条消息
        channel.basicQos(10);

        // exchange autoDelete 是否有绑定, 没有绑定自动删除  queue autoDelete, 没有连接自动删除

        channel.exchangeDeclare("demo-classic-direct-exchange", BuiltinExchangeType.DIRECT,true,false,null);
        channel.exchangeDeclare("demo-quorum-direct-exchange", BuiltinExchangeType.DIRECT,true,false,null);
        channel.exchangeDeclare("demo-stream-direct-exchange", BuiltinExchangeType.DIRECT,true,false,null);

        createClassicQueue(channel,"demo-classic-queue");
        createStreamQueue(channel,"demo-stream-queue");
        createQuorumQueue(channel,"demo-quorum-queue");

        // 消息转发
        channel.exchangeBind("demo-quorum-direct-exchange","demo-classic-direct-exchange","demo");

        channel.queueBind("demo-classic-queue","demo-classic-direct-exchange","demo");
        channel.queueBind("demo-quorum-queue","demo-quorum-direct-exchange","demo");
        channel.queueBind("demo-stream-queue","demo-stream-direct-exchange","demo");


        channel.basicPublish("demo-classic-direct-exchange","demo", MessageProperties.PERSISTENT_TEXT_PLAIN,"chenqf1".getBytes(StandardCharsets.UTF_8));

        // 直接像队列中发送消息
        channel.basicPublish("","demo-classic-direct-exchange",MessageProperties.PERSISTENT_TEXT_PLAIN,"chenqf2".getBytes(StandardCharsets.UTF_8));



        channel.close();
        connection.close();


    }
}
