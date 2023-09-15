package com.maple.user;

import com.maple.user.config.LoadBalancerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;


/**
 * @author chenqf
 */
@SpringBootApplication
@EnableFeignClients
@LoadBalancerClient(value = "spring-cloud-alibaba-stock", configuration = LoadBalancerConfig.class)
public class UserApplication {
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(UserApplication.class, args);
        String userAge = applicationContext.getEnvironment().getProperty("common.age");
        while (true){
            Thread.sleep(3000);
            String property = applicationContext.getEnvironment().getProperty("common.age");
            System.out.println(property);
        }

    }
}
