package com.maple.lock4r.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 陈其丰
 */
@Configuration
public class RedissonConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host +":" + port)
                .setPassword(password)
                .setDatabase(0)
                .setConnectionMinimumIdleSize(50) // 连接池最小空闲数
                .setConnectionPoolSize(100) // 连接池最大线程数
                .setIdleConnectionTimeout(50000) // 线程超时时间
                .setConnectTimeout(20000) // 客户端获取redis连接的超时时间
                .setTimeout(10000); // 响应超时时间
        return Redisson.create(config);
    }
}
