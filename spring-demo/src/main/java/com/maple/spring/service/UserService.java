package com.maple.spring.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author 陈其丰
 */
@Service
@Lazy
@Scope("prototype")
public class UserService implements InitializingBean {

    @Autowired
    private OrderService orderService;

    @Autowired
    public UserService() {
    }


    public void test1(){
        orderService.test1();
    }

    @PostConstruct
    public void beforeInit(){
        // 初始化前
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化
    }
}
