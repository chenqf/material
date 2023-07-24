package com.maple.rabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author 陈其丰
 */
public class Demo {
    public static void main(String[] args) throws IOException, TimeoutException {
        Map<String, String> envVariables = System.getenv();
        String hostname = envVariables.get("ENV_CLOUD_IP");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("chenqf");
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();


        for(int i = 0 ; i < 10000 ; i ++){
            String message = "Sharding message "+i;
            channel.basicPublish("sharding-exchange", String.valueOf(i), null,
                    message.getBytes());
        }



//        Consumer consumer = new DefaultConsumer(channel) {
//            @Override
//            public void handleDelivery(String consumerTag, Envelope envelope,
//                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
//                String message = new String(body, "UTF-8");
//                System.out.println("Received message: " + message);
//            }
//        };
//        channel.basicConsume("classic-demo", true, consumer); // 自动模式
    }
}
