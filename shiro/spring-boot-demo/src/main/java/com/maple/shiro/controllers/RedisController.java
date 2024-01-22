package com.maple.shiro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/set")
    public String set(String key, String value) {
        System.out.println("key = " + key);
        System.out.println("value = " + value);
        stringRedisTemplate.opsForValue().set(key, value);
        return "success";
    }

    @RequestMapping("/get")
    public String get(String key) {
        System.out.println("key = " + key);
        return stringRedisTemplate.opsForValue().get(key);
    }

}
