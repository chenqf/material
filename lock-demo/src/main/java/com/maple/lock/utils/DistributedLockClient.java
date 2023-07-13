package com.maple.lock.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author 陈其丰
 */
@Component
public class DistributedLockClient {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ZookeeperClient zookeeperClient;

    private String uuid;

    public DistributedLockClient() {
        this.uuid = UUID.randomUUID().toString();
    }

    public RedisDistributedLock getRedisLock(String lockName){
        return new RedisDistributedLock(redisTemplate, lockName, uuid);
    }

    public ZookeeperDistributedLock getZookeeperLock(String lockName){
        return new ZookeeperDistributedLock(zookeeperClient.getZookeeper(),lockName);
    }
}
