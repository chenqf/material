package com.maple.flink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * @author chenqf
 */
@SpringBootApplication
@MapperScan("com.maple.flink.mapper")
public class MysqlApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MysqlApplication.class, args);
    }
}
