package com.maple.flink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class FlinkDemo {

    public static void main(String[] args) throws Exception {
        // 流处理-有界流
//        dataStreamApi();
        // 流处理-无界流
        dataStreamUnbounded();
    }

    /**
     * 读文件, 有界流
     */
    public static void dataStreamApi() throws Exception {
        // 1. 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 2. 读取数据
        DataStreamSource<String> lineDs = env.readTextFile("input/word.txt");
        // 3. 处理数据 - 切分 - 转换 - 分组 - 聚合
        SingleOutputStreamOperator<Tuple2<String, Integer>> map = lineDs.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            private static final long serialVersionUID = -1155186844449140190L;

            public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                String[] words = value.split(" ");
                for (String word : words) {
                    Tuple2<String, Integer> t = Tuple2.of(word, 1);
                    // 使用采集器向下游发送数据
                    out.collect(t);
                }
            }
        });
        KeyedStream<Tuple2<String, Integer>, String> keyBy = map.keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
            private static final long serialVersionUID = 51978007575096719L;

            public String getKey(Tuple2<String, Integer> value) throws Exception {
                return value.f0;
            }
        });
        SingleOutputStreamOperator<Tuple2<String, Integer>> sum = keyBy.sum(1);
        // 6. 输出
        sum.print();
        // 7. 执行
        env.execute();
    }

    /**
     * netcat
     * nc -lk 7777
     * 从socket读数据-无界流
     */
    public static void dataStreamUnbounded() throws Exception {
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // IDEA 运行时, 也可以看到webui, 一般用于本地测试 localhost:8081
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        env.setParallelism(3);
        DataStreamSource<String> socketDS = env.socketTextStream("192.168.10.101", 7777);
        SingleOutputStreamOperator<Tuple2<String, Integer>> sum = socketDS.flatMap((String value, Collector<Tuple2<String, Integer>> out) -> {
                    String[] words = value.split(" ");
                    for (String word : words) {
                        Tuple2<String, Integer> t = Tuple2.of(word, 1);
                        // 使用采集器向下游发送数据
                        out.collect(t);
                    }
                })
                .returns(Types.TUPLE(Types.STRING, Types.INT)) // lamada 导致类型擦除, 此处进行指定
                .keyBy(value -> value.f0)
                .sum(1);
        sum.print();
        env.execute();
    }
}
