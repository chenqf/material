package com.maple.spring.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动最后执行
 */
@Component
@Order(2)
public class MyCommandLineRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // 启动参数: --k1=v1 k2=v2
        System.out.println("CommandLineRunner:启动参数: --k1=v1 k2=v2");
        for (String arg : args) {
            System.out.println(arg);
        }
    }
}
