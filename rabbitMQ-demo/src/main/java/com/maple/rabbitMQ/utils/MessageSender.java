package com.maple.rabbitMQ.utils;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 陈其丰
 */
@Component
public class MessageSender {
    public static Map<String, Map<String, Object>> concurrentHashMap = new ConcurrentHashMap<>();
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void rabbitConvertAndSend(String exchange, String routingKey, Object message) {
        this.rabbitConvertAndSend(exchange, routingKey, message, null);

    }

    public void rabbitConvertAndSend(String exchange, String routingKey, Object data, CorrelationData correlationData) {
        String uuid = correlationData == null ? UUID.randomUUID().toString() : correlationData.getId();
        correlationData = correlationData == null ? new CorrelationData(uuid) : correlationData;
        if (concurrentHashMap.containsKey(uuid)) {
            Map<String, Object> map = concurrentHashMap.get(uuid);
            map.put("retry-count", (int) map.get("retry-count") + 1);
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("retry-count", 0);
            map.put("exchange", exchange);
            map.put("routingKey", routingKey);
            map.put("correlationData", correlationData);
            concurrentHashMap.put(uuid, map);
        }
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                concurrentHashMap.get(uuid).put("message",message);
                return message;
            }
        };
        this.rabbitTemplate.convertAndSend(exchange, routingKey, data, messagePostProcessor, correlationData);
    }
}
