package com.maple.shiro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maple.shiro.entity.User;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

    @RequestMapping("/setUser")
    public User set() {
        User user = new User();
        user.setId(123L);
        user.setPwd("123213213");
        user.setName("cqf");
        redisTemplate.opsForValue().set("user", user);
        return user;
    }

    @RequestMapping("/getUser")
    public User get() {
        User user = (User) redisTemplate.opsForValue().get("user");
        return user;
    }

}
