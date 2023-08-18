package com.maple.kafka.kafka;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * @author 陈其丰
 */
public class MyInterceptor implements ProducerInterceptor {
    // 发消息时触发
    @Override
    public ProducerRecord onSend(ProducerRecord producerRecord) {
        return producerRecord;
    }
    // 收到服务端响应时触发
    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }
    // 连接关闭时触发
    @Override
    public void close() {

    }
    // 整理配置项
    @Override
    public void configure(Map<String, ?> map) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            System.out.println("entry.key:" + entry.getKey() + " === entry.value:" + entry.getValue());
        }
    }
}
