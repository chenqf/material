package com.maple.flink.source;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class FromKafka {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        // 从Kafka中读
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("62.234.18.108:9092")
                .setGroupId("chenqf-kafka-group") // 消费者组
                .setTopics("demoTopic1") // topic
                .setValueOnlyDeserializer(new SimpleStringSchema()) // 反序列化
                .setStartingOffsets(OffsetsInitializer.latest())// 起始位置 默认:earliest 一定从最早开始 latest 一定从最新消费 (与Kafka不一致)
                .build();

        DataStreamSource<String> fromKafka = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "fromKafka");
        fromKafka.print();

        env.execute();

//        // 1.获取执行环境
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//
//        // 2.读取kafka数据
//        KafkaSource<String> source = KafkaSource.<String>builder()
//                .setBootstrapServers("62.234.18.108:9092")               // 必填：指定broker连接信息 (为保证高可用,建议多指定几个节点)
//                .setTopics("demoTopic1")                              // 必填：指定要消费的topic
//                .setGroupId("FlinkConsumer")                        // 必填：指定消费者的groupid(不存在时会自动创建)
//                .setValueOnlyDeserializer(new SimpleStringSchema()) // 必填：指定反序列化器(用来解析kafka消息数据)
//                .setStartingOffsets(OffsetsInitializer.latest())  // 可选：指定启动任务时的消费位点（不指定时，将默认使用 OffsetsInitializer.earliest()）
//                .build();
//
//        DataStreamSource<String> fromKafka = env.fromSource(source,
//                WatermarkStrategy.noWatermarks(),
//                "Kafka Source");
//        fromKafka.print();
//
//        // 3.触发程序执行
//        env.execute();
    }
}
