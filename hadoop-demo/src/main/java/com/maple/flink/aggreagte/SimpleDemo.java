package com.maple.flink.aggreagte;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.maple.entity.WaterSensor;

public class SimpleDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("s1", 1L, 1),
                new WaterSensor("s1", 2L, 2),
                new WaterSensor("s2", 3L, 3),
                new WaterSensor("s3", 4L, 4),
                new WaterSensor("s3", 5L, 5)
        );
        KeyedStream<WaterSensor, String> keyBy = sensorDS.keyBy(new KeySelector<WaterSensor, String>() {
            private static final long serialVersionUID = -8029649922091486537L;

            @Override
            public String getKey(WaterSensor waterSensor) throws Exception {
                return waterSensor.getId();
            }
        });

        keyBy.sum("vc").print();

        env.execute();
    }
}