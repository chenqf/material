package com.maple.flink.aggreagte;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.maple.entity.WaterSensor;

public class ReduceDemo {
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

        // 每个keyBy分组第一条数据, 不会进入reduce回调中
        keyBy.reduce(new ReduceFunction<WaterSensor>() {
            private static final long serialVersionUID = 8537726912908479085L;

            // prev: 之前的计算结果
            // current: 当前记录
            @Override
            public WaterSensor reduce(WaterSensor prev, WaterSensor current) throws Exception {
                System.out.println("prev:" + prev);
                System.out.println("current:" + current);
                return new WaterSensor(prev.getId(), prev.getTs() + current.getTs(), prev.getVc() + current.getVc());
            }
        }).print();

        env.execute();
    }
}
