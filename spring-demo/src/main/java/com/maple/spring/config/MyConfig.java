package com.maple.spring.config;

import com.maple.spring.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author 陈其丰
 */
@Configuration
public class MyConfig {

    // 当前容器中没有UserService对应的Bean则生成对应的bean
    @Bean
    @ConditionalOnMissingBean(UserService.class)
    public UserService userService(){
        return new UserService();
    }
}
