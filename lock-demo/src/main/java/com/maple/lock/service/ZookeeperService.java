package com.maple.lock.service;

import com.maple.lock.utils.DistributedLockClient;
import com.maple.lock.utils.ZookeeperClient;
import com.maple.lock.utils.ZookeeperDistributedLock;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈其丰
 */
@Service
public class ZookeeperService {

    @Autowired
    private DistributedLockClient distributedLockClient;

    @Autowired
    private StockService stockService;

    public void deduct(){
        ZookeeperDistributedLock lock = distributedLockClient.getZookeeperLock("lock");
        lock.lock();
        this.stockService.redisDeduct();
        lock.unlock();
    }

}
