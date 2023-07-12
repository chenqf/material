package com.maple.lock.controller;

import com.maple.common.domain.Result;
import com.maple.lock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */
@RequestMapping("/stock")
@RestController
public class StockController {
    @Autowired
    private StockService stockService;

    @GetMapping("/deduct")
    public Result deduct(){
        this.stockService.deduct();
        return Result.success();
    }

    @GetMapping("/synchronizedDeduct")
    public Result synchronizedDeduct(){
        this.stockService.synchronizedDeduct();
        return Result.success();
    }

    @GetMapping("/reentrantLockDeduct")
    public Result reentrantLockDeduct(){
        this.stockService.reentrantLockDeduct();
        return Result.success();
    }

    @GetMapping("/pessimisticSqlDeduct")
    public Result pessimisticSqlDeduct(){
        this.stockService.pessimisticSqlDeduct();
        return Result.success();
    }

    @GetMapping("/pessimisticSelectForUpdateDeduct")
    public Result pessimisticSelectForUpdateDeduct(){
        this.stockService.pessimisticSelectForUpdateDeduct();
        return Result.success();
    }
    @GetMapping("/optimisticDeduct")
    public Result optimisticDeduct() throws InterruptedException {
        this.stockService.optimisticDeduct();
        return Result.success();
    }

    @GetMapping("/redisOptimisticDeduct")
    public Result redisOptimisticDeduct() throws InterruptedException {
        this.stockService.redisOptimisticDeduct();
        return Result.success();
    }
    @GetMapping("/redisDistributedLockDeduct")
    public Result redisDistributedLockDeduct() throws InterruptedException {
        this.stockService.redisDistributedLockDeduct();
        return Result.success();
    }
    @GetMapping("/redisLuaDistributedLockDeduct")
    public Result redisLuaDistributedLockDeduct() throws InterruptedException {
        this.stockService.redisLuaDistributedLockDeduct();
        return Result.success();
    }
    @GetMapping("/finalRedisDistributedLockDeduct")
    public Result finalRedisDistributedLockDeduct() throws InterruptedException {
        this.stockService.finalRedisDistributedLockDeduct();
        return Result.success();
    }
}
