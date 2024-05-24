package com.maple.kafka.kafka;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * @author 陈其丰
 */
public class MyProducer {
    //    private static final String BOOTSTRAP_SERVERS = System.getenv().get("ENV_CLOUD_IP") + ":9092";
    private static final String BOOTSTRAP_SERVERS = "62.234.18.108:9092";
    ;
    private static final String TOPIC_NAME = "demoTopic1";

    private static final String messageKey = "message_key";

    private static final String messageValue = "message_value";

    private static final String sendType = "direct"; // direct sync async

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // PART1:设置发送者相关属性
        Properties props = new Properties();
        // 此处配置的是kafka的端口
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // 配置key的序列化类
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // 配置value的序列化类
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 配置生产者拦截器
//        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,"com.maple.kafka.kafka.MyInterceptor");
        // 配置生产者消息缓存-无负载时增加最多X毫秒的延时
//        props.put(ProducerConfig.LINGER_MS_CONFIG,20);
        // 配置生产者消息路由
//        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,"com.maple.kafka.kafka.MyPartitioner");

        Producer<String, String> producer = new KafkaProducer<>(props);
        // Part2:构建消息 - topic不存在自动创建 - 第三个参数是真正的value
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, messageKey + UUID.randomUUID().toString(), messageValue + UUID.randomUUID().toString());
        // Part3:发送消息
        if (sendType.equals("direct")) { // 单向发送：不关心服务端的应答。
            producer.send(record);
            producer.close();
        } else if (sendType.equals("sync")) { // 同步发送：获取服务端应答消息前，会阻塞当前线程。
            RecordMetadata recordMetadata = producer.send(record).get();
            String topic = recordMetadata.topic();
            int partition = recordMetadata.partition();
            long offset = recordMetadata.offset();
            String message = recordMetadata.toString();
            System.out.println("message:[" + message + "] send with topic:" + topic + ";partition:" + partition + ";offset:" + offset);
            producer.close();
        } else if (sendType.equals("async")) { // // 异步发送：消息发送后不阻塞，服务端有应答后会触发回调函数
            CountDownLatch latch = new CountDownLatch(1);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (null != e) {
                        System.out.println("消息发送失败," + e.getMessage());
                        e.printStackTrace();
                    } else {
                        String topic = recordMetadata.topic();
                        long offset = recordMetadata.offset();
                        String message = recordMetadata.toString();
                        System.out.println("message:[" + message + "] send with opic:" + topic + ";offset:" + offset);
                    }
                    latch.countDown();
                }
            });
            latch.await();
            producer.close();
        }
    }
}
