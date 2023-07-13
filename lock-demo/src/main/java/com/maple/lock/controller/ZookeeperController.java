package com.maple.lock.controller;

import com.maple.common.domain.Result;
import com.maple.lock.service.StockService;
import com.maple.lock.service.ZookeeperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */
@RequestMapping("/zookeeper")
@RestController
public class ZookeeperController {
    @Autowired
    private ZookeeperService zookeeperService;


    @GetMapping("/deduct")
    public Result deduct() {
        this.zookeeperService.deduct();
        return Result.success();
    }
}
