package com.maple.sharding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * @author chenqf
 */
@SpringBootApplication
@MapperScan("com.maple.sharding.mapper")
public class ShardingApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(ShardingApplication.class, args);
    }
}
