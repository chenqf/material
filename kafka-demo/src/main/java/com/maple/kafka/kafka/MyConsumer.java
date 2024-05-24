package com.maple.kafka.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * @author 陈其丰
 */
public class MyConsumer {
    //    private static final String BOOTSTRAP_SERVERS = System.getenv().get("ENV_CLOUD_IP") + ":9092";
    private static final String BOOTSTRAP_SERVERS = "62.234.18.108:9092";
    private static final String TOPIC_NAME = "demoTopic1";
    private static final String CONSUMER_GROUP_NAME="demoGroup";

    public static void main(String[] args) throws InterruptedException {
        // PART1:设置发送者相关属性
        Properties props = new Properties();
        // kafka地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // 每个消费者要指定一个group **必须**
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_NAME);
        // key序列化类
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // value序列化类
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        // 订阅主题
        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        while (true){
            // PART2:拉取消息
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofNanos(100)); // 100毫秒超时时间
            // PART3:处理消息
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("offset = " + record.offset() + ";key = " + record.key() + "; value= " + record.value());
            }
            consumer.commitSync(); //同步提交，表示必须等到offset提交完毕，再去消费下一批数据。
            // consumer.commitAsync(); //异步提交，表示发送完提交offset请求后，就开始消费下一批数据了。不用等到Broker的确认
            Thread.sleep(2000);
        }
        // 关闭连接, 视情况而用
        // consumer.close();

    }
}
