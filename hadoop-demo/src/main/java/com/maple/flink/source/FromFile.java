package com.maple.flink.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FromFile {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 从文件中读
        FileSource<String> source = FileSource.forRecordStreamFormat(new TextLineInputFormat(), new Path("C:\\work\\java-workspace\\material\\hadoop-demo\\src\\main\\java\\com\\maple\\flink\\source\\word.txt")).build();
        DataStreamSource<String> fromFile = env.fromSource(source, WatermarkStrategy.noWatermarks(), "fromFile");
        fromFile.print();

        env.execute();
    }
}
