package com.maple.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * @author chenqf
 */
@SpringBootApplication
@EnableCaching
public class CacheApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(CacheApplication.class, args);
    }
}
