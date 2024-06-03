package com.maple.flink.transform;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import com.maple.entity.WaterSensor;

// 如果输入是 s1 , 打印 vc, 如果是 s2 打印 vc ts
public class FlatMapDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("s1", 1L, 1),
                new WaterSensor("s1", 11L, 11),
                new WaterSensor("s2", 2L, 2),
                new WaterSensor("s2", 22L, 22)
        );

        // 方式一: 匿名内部类
        SingleOutputStreamOperator<String> flatMap = sensorDS.flatMap(new FlatMapFunction<WaterSensor, String>() {
            private static final long serialVersionUID = -8403006960028838517L;

            @Override
            public void flatMap(WaterSensor waterSensor, Collector<String> out) throws Exception {
                if ("s1".equals(waterSensor.getId())) {
                    out.collect(waterSensor.getVc().toString());
                } else if ("s2".equals(waterSensor.getId())) {
                    out.collect(waterSensor.getVc().toString());
                    out.collect(waterSensor.getTs().toString());
                }
            }
        });


        flatMap.print();

        env.execute();
    }
}
