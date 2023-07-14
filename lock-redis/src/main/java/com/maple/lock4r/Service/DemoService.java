package com.maple.lock4r.Service;

import com.maple.lock4r.annotation.AutoLock4r;
import com.maple.lock4r.utils.LockClient;
import com.maple.lock4r.utils.LockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈其丰
 */
@Service
public class DemoService {

    @Autowired
    private LockClient lockClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void test1() throws InterruptedException {
        LockManager redisLock = lockClient.getRedisLock("lock-key");
        try {
            redisLock.lock();
            // 业务逻辑-------------------
            String key = "demo1";
            String s = this.redisTemplate.opsForValue().get(key);
            if(s == null || s.length() == 0){
                this.redisTemplate.opsForValue().set(key, String.valueOf(1));
            }else{
                int i = Integer.parseInt(s);
                this.redisTemplate.opsForValue().set(key, String.valueOf(i + 1));
            }
        }finally {
            redisLock.unlock();
        }
    }

    @AutoLock4r
    public void test2() {
        String key = "demo2";
        String s = this.redisTemplate.opsForValue().get(key);
        if(s == null || s.length() == 0){
            this.redisTemplate.opsForValue().set(key, String.valueOf(1));
        }else{
            int i = Integer.parseInt(s);
            this.redisTemplate.opsForValue().set(key, String.valueOf(i + 1));
        }
    }
}
