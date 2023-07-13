package com.maple.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.maple.lock.mapper.StockMapper;
import com.maple.lock.pojo.Stock;
import com.maple.lock.utils.DistributedLockClient;
import com.maple.lock.utils.RedisDistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 陈其丰
 */
@Service
public class RedissonService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    public void deduct(){
        String stock = this.redisTemplate.opsForValue().get("stock");
        if (stock != null && stock.length() != 0) {
            int count = Integer.parseInt(stock);
            if(count > 0){
                this.redisTemplate.opsForValue().set("stock", String.valueOf(--count));
            }
        }
    }

    public void redissonDeduct(){
        RLock lock = this.redissonClient.getLock("lock-key"); // 非公平锁
//        RLock lock = this.redissonClient.getFairLock("lock-key"); // 公平锁
        lock.lock();
        try{
            System.out.println("1111111111111");
            this.deduct();
        }finally {
            lock.unlock();
        }
    }

    public void testWriteLock(){
        RReadWriteLock lock = this.redissonClient.getReadWriteLock("rwLock");
        lock.writeLock().lock();
        //......read
        lock.writeLock().unlock();
    }

    public void testReadLock(){
        RReadWriteLock lock = this.redissonClient.getReadWriteLock("rwLock");
        lock.readLock().lock();
        //......write
        lock.readLock().unlock();
    }

    public void testSemaphore(){
        RSemaphore semaphore = this.redissonClient.getSemaphore("semaphore");
        semaphore.trySetPermits(3); // 设置资源量 限流的线程数
        try {
            semaphore.acquire(); // 获取资源, 获取成功的线程继续执行,否则被阻塞
            System.out.println("执行业务.....");
            TimeUnit.SECONDS.sleep(10 + new Random().nextInt(10));
            System.out.println("业务执行完....");
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testCountDownLatch(){

    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            new Thread(()->{
                System.out.println(Thread.currentThread().getName() + " 要出门了");
                try {
                    TimeUnit.SECONDS.sleep(new Random().nextInt(5));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println(Thread.currentThread().getName() + " 已经出门了");
                    countDownLatch.countDown();
                }
            }).start();
        }
        countDownLatch.wait();
        System.out.println(Thread.currentThread().getName() + " 班长锁门");
    }
}
