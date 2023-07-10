package com.maple.redis.controller;

import com.maple.common.domain.Result;
import com.maple.redis.pojo.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqf
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @RequestMapping("/set")
    public Result set(){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set("user.age","20");

        return Result.success(1);
    }
    @RequestMapping("/get")
    public Result get(){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        return Result.success(operations.get("use.age"));
    }

    @RequestMapping("/setObject")
    public Result setObject(){
        Employee chenqf = new Employee(1, "chenqf1006");
        this.redisTemplate.opsForValue().set("employee",chenqf);
        System.out.println("set object: " + chenqf);
        return Result.success(chenqf);
    }
}
