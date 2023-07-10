package com.maple.cache.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author chenqf
 */
@Service
public class DemoService {


    /**
     * @Cacheable
     * 0. value必填不可为空
     * 1. 方法调用之前, 先根据value查找Cache,第一次获取不到就创建
     * 2. 去value对应的Cache中根据key查找entry, key使用方法中的参数来生成
     *      key是按某种规则创建的, 默认为SimpleKeyGenerator
     *          无参数, key = new SimpleKey();
     *          1个参数, key = 参数的值
     *          n个参数, key = new SimpleKey(params);
     * 3. 如果查到, 直接返回, 没有查到就调用目标方法
     * 4. 将目标方法返回的结果放到value对应的Cache中
     */
    @Cacheable(value = "cacheName1")
    public String testCache1(){
        System.out.println("enter testCache1 method");
        return new Date().toString();
    }

    /**
     * key 可自定义, 语法为 SpEL 表达式
     */
    @Cacheable(value = "cacheName1",key = "#root.methodName")
    public String testCache2(){
        System.out.println("enter testCache2 method");
        return new Date().toString();
    }

    @Cacheable(value = "cacheName1",keyGenerator = "myKeyGenerator")
    public String testCache3(){
        System.out.println("enter testCache3 method");
        return new Date().toString();
    }

    @Cacheable(value = "cacheName1", condition = "#a0 >1")
    public String testCache4(){
        System.out.println("enter testCache4 method");
        return new Date().toString();
    }

    @Cacheable(value = "cacheName1", unless = "#a0 >1")
    public String testCache5(){
        System.out.println("enter testCache5 method");
        return new Date().toString();
    }
}
