package com.maple.flink.transform;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.maple.entity.WaterSensor;

public class MapDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("s1", 1L, 1),
                new WaterSensor("s2", 2L, 2),
                new WaterSensor("s3", 3L, 3)
        );

        // 方式一: 匿名内部类
//        SingleOutputStreamOperator<String> map = sensorDS.map(new MapFunction<WaterSensor, String>() {
//            private static final long serialVersionUID = -8875810405517918745L;
//
//            @Override
//            public String map(WaterSensor value) throws Exception {
//                return value.getId();
//            }
//        });

        // 方式二: lambda表达式
        SingleOutputStreamOperator<String> map = sensorDS.map(WaterSensor::getId);

        map.print();

        env.execute();
    }
}
