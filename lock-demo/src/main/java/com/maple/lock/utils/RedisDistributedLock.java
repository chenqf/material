package com.maple.lock.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author 陈其丰
 */
public class RedisDistributedLock implements Lock {

    private StringRedisTemplate redisTemplate;
    private String lockName;

    private String uuid; // server process id

    private long expire = 30; // seconds

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockName, String serverUuid) {
        this.redisTemplate = redisTemplate;
        this.lockName = lockName;
        this.uuid = serverUuid + ":" + Thread.currentThread().getId();
    }

    private void renewExpire(){
        String script = "if redis.call('hexists', KEYS[1], ARGV[1]) == 1 " +
                "then " +
                    "return redis.call('expire', KEYS[1], ARGV[2]) " +
                "else " +
                    "return 0 " +
                "end";
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList(lockName), uuid, String.valueOf(expire))) {
                    renewExpire();
                }
            }
        },expire / 3 * 1000);
    }

    @Override
    public void lock() {
        this.tryLock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        try {
            return this.tryLock(-1L,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, @NotNull TimeUnit unit) throws InterruptedException {
        if(time != -1L){
            this.expire = unit.toSeconds(time);
        }
        String script = "if redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1 " +
                "then " +
                    "redis.call('hincrby', KEYS[1], ARGV[1], 1) " +
                    "redis.call('expire', KEYS[1], ARGV[2]) " +
                    "return 1 " +
                "else " +
                    "return 0 " +
                "end";
        while (!this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList(lockName), uuid, String.valueOf(expire))){
            Thread.sleep(50);
        }
        // 加锁成功,开启定时器自动续期
        this.renewExpire();
        return true;
    }

    @Override
    public void unlock() {
        String script = "if redis.call('hexists', KEYS[1], ARGV[1]) == 0 " +
                "then " +
                    "return nil " +
                "elseif redis.call('hincrby', KEYS[1], ARGV[1], -1) == 0 " +
                "then " +
                    "return redis.call('del',KEYS[1]) " +
                "else " +
                    "return 0 " +
                "end";

        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);
        if(flag == null){
            throw new IllegalMonitorStateException("You don't have this lock!");
        }
    }

    @NotNull
    @Override
    public Condition newCondition() {
        return null;
    }
}
