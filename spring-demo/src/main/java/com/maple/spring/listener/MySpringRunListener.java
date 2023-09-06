package com.maple.spring.listener;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;

/**
 * @author 陈其丰
 */
public class MySpringRunListener implements SpringApplicationRunListener{

    private SpringApplication application;

    private String[] args;

    public MySpringRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        System.out.println("开始启动");
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        System.out.println("环境准备完成:配置参数");
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        System.out.println("容器创建完成");
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        System.out.println("配置类注册完成");
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        System.out.println("容器启动完成,WEB服务器启动完成,ApplicationRunner和CommandLineRunner还未执行");
    }


    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
        System.out.println("容器准备好了,ApplicationRunner和CommandLineRunner执行完了");
    }


    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        System.out.println("容器创建失败");
    }
}
