package com.maple.rabbitMQ.config;


import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.maple.rabbitMQ.utils.MessageSender;

/**
 * @author 陈其丰
 */
@Configuration
public class RabbitConfig {
    // 失败时重复调用次数
    private final static int RETRY_COUNT = 5;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MessageSender messageSender;

    @Autowired
    ConnectionFactory connectionFactory;


    @PostConstruct
    public void init() {
        rabbitTemplate.setMandatory(true);
        // 确认消息送到交换机(Exchange)回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) {
                return;
            }
            if (correlationData.getId().isEmpty()) {
                return;
            }
            String msgId = correlationData.getId();
            if (ack) {
                MessageSender.concurrentHashMap.remove(msgId);
            } else if (MessageSender.concurrentHashMap.containsKey(msgId)) {
                Map<String, Object> map = MessageSender.concurrentHashMap.get(msgId);
                int retryCount = (int) map.get("retry-count");
                Message message = (Message) map.get("message");
                String exchange = (String) map.get("exchange");
                String routingKey = (String) map.get("routingKey");
                // 在重试次数内 - 重新发送
                if (retryCount < RETRY_COUNT) {
                    messageSender.rabbitConvertAndSend(exchange, routingKey, message, correlationData);
                } else {
                    message.getMessageProperties().setHeader("previous-exchange", exchange);
                    message.getMessageProperties().setHeader("previous-routingKey", routingKey);
                    message.getMessageProperties().setHeader("retry-count", retryCount + 1);
//                    rabbitTemplate.convertAndSend("exchange-spare", "routingKey", message);
                    rabbitTemplate.convertAndSend("exchange-demo", "demo", message);
                }
            }
        });

        // exchange -> queue 错误回调
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            Message message = returnedMessage.getMessage();
            Object o = message.getMessageProperties().getHeaders().get("retry-count");
            long retryCount = o == null ? 0 : (long) o;
            // 在重试次数内 - 重新发送
            if (retryCount < RETRY_COUNT) {
                message.getMessageProperties().setHeader("retry-count", retryCount + 1);
                rabbitTemplate.convertAndSend(returnedMessage.getExchange(), returnedMessage.getRoutingKey(), returnedMessage.getMessage());
            }
            // 将原 exchange routingKey 通过 header 保存, 将消息发送到备用队列
            else {
                message.getMessageProperties().setHeader("previous-exchange", returnedMessage.getExchange());
                message.getMessageProperties().setHeader("previous-routingKey", returnedMessage.getRoutingKey());
//                rabbitTemplate.convertAndSend("exchange-spare", "routingKey", returnedMessage.getMessage());
                rabbitTemplate.convertAndSend(returnedMessage.getExchange(), "demo", returnedMessage.getMessage());
            }
        });
    }


}
