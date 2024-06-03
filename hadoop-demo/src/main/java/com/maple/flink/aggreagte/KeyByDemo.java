package com.maple.flink.aggreagte;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.maple.entity.WaterSensor;

// 按照Id进行分组
public class KeyByDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("s1", 1L, 1),
                new WaterSensor("s2", 2L, 2),
                new WaterSensor("s3", 3L, 3),
                new WaterSensor("s4", 4L, 4)
        );

        // 按照Id进行分组
        // 不是转换算子, 只是重分区, 不能设置并行度
        KeyedStream<WaterSensor, String> keyBy = sensorDS.keyBy(new KeySelector<WaterSensor, String>() {
            private static final long serialVersionUID = -8029649922091486537L;

            @Override
            public String getKey(WaterSensor waterSensor) throws Exception {
                return waterSensor.getId();
            }
        });

        keyBy.print();

        env.execute();
    }
}
