package com.maple.lock.controller;

import com.maple.common.domain.Result;
import com.maple.lock.pojo.Stock;
import com.maple.lock.service.RedissonService;
import com.maple.lock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */
@RequestMapping("/redisson")
@RestController
public class RedissonController {
    @Autowired
    private RedissonService redissonService;

    @GetMapping("/test/read/lock")
    public Result testReadLock() throws InterruptedException {
        this.redissonService.testReadLock();
        return Result.success(null);
    }

    @GetMapping("/test/write/lock")
    public Result testWriteLock() throws InterruptedException {
        this.redissonService.testWriteLock();
        return Result.success(null);
    }
    @GetMapping("/testLatch")
    public Result testLatch() throws InterruptedException {
        this.redissonService.testLatch();
        return Result.success("锁上门了");
    }

    @GetMapping("/testCountDown")
    public Result testCountDown() throws InterruptedException {
        this.redissonService.testCountDown();
        return Result.success("出门了");
    }

    @GetMapping("/testAnnotation")
    public Result testAnnotation() {
        Stock stock = new Stock(10L,"10001","大连",100,1);
        this.redissonService.testAnnotation(stock);
        return Result.success();
    }
}
