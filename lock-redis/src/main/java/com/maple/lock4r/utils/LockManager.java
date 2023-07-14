package com.maple.lock4r.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author 陈其丰
 */

public class LockManager {

    private StringRedisTemplate redisTemplate;

    private String uuid; // server process id

    private long expire = 30; // seconds
    private String lockName;

    public LockManager(StringRedisTemplate redisTemplate, String uuid, String lockName) {
        this.redisTemplate = redisTemplate;
        this.uuid = uuid + Thread.currentThread().getId(); // 确保锁可重入
        this.lockName = lockName;
    }

    /**
     * 续期-看门狗
     */
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

    public void lock() throws InterruptedException {
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
    }

    public void unlock(){
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
}
