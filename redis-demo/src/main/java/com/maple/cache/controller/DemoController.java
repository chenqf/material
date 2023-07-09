package com.maple.cache.controller;

import com.maple.common.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/test1")
    public Result teset1(){
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set("username","chenqf");

        return Result.success(1);
    }
    @RequestMapping("/test2")
    public Result test2(){
        ValueOperations operations = redisTemplate.opsForValue();
        return Result.success(operations.get("username"));
    }


    @RequestMapping("/test3")
    public Result teset3(){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set("user.age","20");

        return Result.success(1);
    }
    @RequestMapping("/test4")
    public Result test4(){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        return Result.success(operations.get("use.age"));
    }
}
