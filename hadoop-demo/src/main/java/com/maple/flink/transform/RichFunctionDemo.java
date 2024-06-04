package com.maple.flink.transform;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class RichFunctionDemo {
    public static void main(String[] args) throws Exception {

        /**
         * RichXxxFunction : 富函数
         * 1. 多了生命周期管理函数
         *      open() : 每个子任务, 在启动时调用一次
         *      close() : 每个子任务, 在结束时调用一次
         * 2. 多了一个运行时上下文
         *      可以获取一些运行时的环境信息, 比如: 子任务编号/名称/....
         */
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Integer> source = env.fromElements(1, 2, 3, 4, 5, 6);

        SingleOutputStreamOperator<Integer> map = source.map(new RichMapFunction<Integer, Integer>() {
            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);

                RuntimeContext runtimeContext = getRuntimeContext();
                int indexOfThisSubtask = runtimeContext.getIndexOfThisSubtask();
                String taskNameWithSubtasks = runtimeContext.getTaskNameWithSubtasks();
                System.out.println("indexOfThisSubtask = " + indexOfThisSubtask + "subTask Name=" + taskNameWithSubtasks + ", launch open()");
            }

            @Override
            public void close() throws Exception {
                super.close();
                RuntimeContext runtimeContext = getRuntimeContext();
                int indexOfThisSubtask = runtimeContext.getIndexOfThisSubtask();
                String taskNameWithSubtasks = runtimeContext.getTaskNameWithSubtasks();
                System.out.println("indexOfThisSubtask = " + indexOfThisSubtask + "subTask Name=" + taskNameWithSubtasks + ", launch close()");
            }

            @Override
            public Integer map(Integer integer) throws Exception {
                return integer;
            }

            private static final long serialVersionUID = -1345222016387322730L;
        });

        map.print();
        env.execute();
    }
}
