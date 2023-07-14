package com.maple.lock4r.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author 陈其丰
 */
@Component
public class LockClient {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String uuid;

    public LockClient() {
        // LockClient 为单例,uuid在当前服务进程内保持不变
        this.uuid = UUID.randomUUID().toString();
    }

    public LockManager getRedisLock(String lockName) {
        return new LockManager(redisTemplate, uuid, lockName);
    }
}
