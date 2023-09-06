package com.maple.spring;

import com.maple.spring.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author 陈其丰
 */
public class Test {
    public static void main(String[] args) {
        //通过配置类创建并获取容器
        AnnotationConfigApplicationContext annotationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        //通过XML创建并获取容器
        ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext("spring.xml");

        //通过容器获取Bean
        UserService bean1 = (UserService) annotationContext.getBean("userService");
        UserService bean2 = (UserService) xmlContext.getBean("userService");
    }
}
