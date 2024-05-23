package com.maple.flink;


import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class EnvDemo {
    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration(); // 用于修改一些参数

        conf.set(RestOptions.BIND_PORT, "8081");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);

        // 流批一体, 默认为流方式
        // 此种方式不常用, 一般通过命令行配置来进行修改 -Dexecution.runtime-mode=BATCH
        // env.setRuntimeMode(RuntimeExecutionMode.BATCH); // 批处理方式来执行

        //....

        // 懒执行 / 延迟执行
        env.execute();  // 必须写
    }
}
