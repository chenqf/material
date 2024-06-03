package com.maple.flink.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FromDataGen {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 如果有多个并行度, 将数值均分成n份
        env.setParallelism(2);
        /**
         * 数据生成器
         * 参数:
         * 1. GeneratorFunction接口, 重写map方法, 输入类型固定是Long
         * 2. Long类型, 自动生成的数字序列, 从1自增
         * 3. 限速策略, eg: 每秒几条
         * 4. 返回的类型
         */
        DataGeneratorSource<String> source = new DataGeneratorSource<>(
                // GeneratorFunction接口, 重写map方法, 输入类型固定是Long
                new GeneratorFunction<Long, String>() {
                    private static final long serialVersionUID = 7096925423962680215L;

                    @Override
                    public String map(Long value) throws Exception {
                        return "Number" + value;
                    }
                },
                10, // Long类型, 自动生成的数字序列, 从0自增 Long.MAX_VALUE : 实现无界流
                RateLimiterStrategy.perSecond(1), // 限速策略, eg: 每秒几条
                Types.STRING // 返回的类型
        );

        env.fromSource(source, WatermarkStrategy.noWatermarks(), "data-generator").print();

        env.execute();
        
    }
}
