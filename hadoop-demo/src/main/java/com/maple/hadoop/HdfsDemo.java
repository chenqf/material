package com.maple.hadoop;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;


public class HdfsDemo {

    private FileSystem fs;


    //    @Before
    public void init() throws URISyntaxException, IOException, InterruptedException {
        Configuration configuration = new Configuration();

        fs = FileSystem.get(new URI("hdfs://192.168.10.101:8020"),
                configuration, "chenqf");
    }

    //    @After
    public void close() throws IOException {
        // 3 关闭资源
        fs.close();
    }

    //    @Test
    public void testMkdir() throws IOException {
        fs.mkdirs(new Path("/xiyou1/"));
    }

    //    @Test
    public void testPut() throws IOException {
        fs.copyFromLocalFile(false, true, new Path("D:\\hdfs-demo.txt"), new Path("hdfs://server1/xiyou/huaguoshan/"));
    }

    //    @Test
    public void testGet() throws IOException {
        fs.copyToLocalFile(false, new
                        Path("/xiyou/huaguoshan/hdfs-demo.txt"), new Path("d:/sunwukong2.txt"),
                true);
    }

}
