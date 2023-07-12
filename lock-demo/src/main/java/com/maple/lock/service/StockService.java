package com.maple.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.maple.lock.mapper.StockMapper;
import com.maple.lock.pojo.Stock;
import com.maple.lock.utils.DistributedLockClient;
import com.maple.lock.utils.RedisDistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 陈其丰
 */
@Service
public class StockService {
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLockClient distributedLockClient;

    private ReentrantLock lock = new ReentrantLock();

    public synchronized void synchronizedDeduct(){
        this.deduct();
    }
    public void reentrantLockDeduct(){
        lock.lock();
        try{
            this.deduct();
        }finally {
            lock.unlock();
        }

    }

    public void deduct(){
        Stock stock = this.stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
        if(stock != null && stock.getCount() > 0){
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }
    }

    public void pessimisticSqlDeduct(){
        this.stockMapper.pessimisticSqlDeduct(1, 1);
    };

    @Transactional
    public void pessimisticSelectForUpdateDeduct(){
        Stock stock = this.stockMapper.pessimisticSelectForUpdateDeduct(1);
        if(stock != null && stock.getCount() >= 1){
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }
    };

    public void optimisticDeduct() throws InterruptedException {
        Stock stock = this.stockMapper.selectById(1);
        Integer version = stock.getVersion();
        stock.setCount(stock.getCount() - 1);
        stock.setVersion(version + 1);
        UpdateWrapper<Stock> updateWrapper = new UpdateWrapper<Stock>()
                .eq("id", stock.getId())
                .eq("version", version);
        int num = this.stockMapper.update(stock, updateWrapper);
        if(num == 0){
            // 可重试
            Thread.sleep(20);
            this.optimisticDeduct();
        }
    }

    public void redisDeduct(){
        String stock = this.redisTemplate.opsForValue().get("stock");
        if (stock != null && stock.length() != 0) {
            int count = Integer.parseInt(stock);
            if(count > 0){
                this.redisTemplate.opsForValue().set("stock", String.valueOf(--count));
            }
        }
    }

    public void redisOptimisticDeduct() throws InterruptedException {
        Boolean execute = this.redisTemplate.execute(new SessionCallback<Boolean>() {
            @Override
            public Boolean execute(RedisOperations operations) throws DataAccessException {
                operations.watch("stock");
                String stock = operations.opsForValue().get("stock").toString();
                if (stock != null && stock.length() != 0) {
                    int count = Integer.parseInt(stock);
                    if (count > 0) {
                        operations.multi();
                        operations.opsForValue().set("stock", String.valueOf(--count));
                        List exec = operations.exec();
                        // 如果事务的返回值为空, 在代表减库存失败,重试
                        return exec != null && exec.size() >0;
                    }
                }
                return true;
            }
        });
        if(!execute){
            Thread.sleep(20);
            this.redisOptimisticDeduct();
        }
    }

    public void redisDistributedLockDeduct() throws InterruptedException {
        String uuid = UUID.randomUUID().toString();
        String lockKey = "lock";
        // 加锁
        while (Boolean.FALSE.equals(this.redisTemplate.opsForValue().setIfAbsent(lockKey, uuid,10, TimeUnit.SECONDS))) {
            Thread.sleep(50);
        }
        try{
            this.redisDeduct();
        }finally {
            // 解锁 - 只能解自己的锁
            if (StringUtils.equals(this.redisTemplate.opsForValue().get(lockKey),uuid)) {
                this.redisTemplate.delete(lockKey);
            }
        }

    }

    public void redisLuaDistributedLockDeduct() throws InterruptedException {
        String uuid = UUID.randomUUID().toString();
        String lockKey = "lock";
        // 加锁 --- 防止死锁(加锁后宕机):设置过期时间
        while (Boolean.FALSE.equals(this.redisTemplate.opsForValue().setIfAbsent(lockKey, uuid,10, TimeUnit.SECONDS))) {
            Thread.sleep(50);
        }
        try{
            this.redisDeduct();
        }finally {
            // 解锁 - 只能解自己的锁
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] " +
                    "then " +
                        "return redis.call('del',KEYS[1]) " +
                    "else " +
                        "return 0 " +
                    "end";
            this.redisTemplate.execute(new DefaultRedisScript(script,Boolean.class), Arrays.asList(lockKey),uuid);
        }
    }
    public void finalRedisDistributedLockDeduct() {
        RedisDistributedLock redisLock = this.distributedLockClient.getRedisLock("lock-demo");
        redisLock.lock();
        try {
            this.redisDeduct();
        } finally {
            redisLock.unlock();
        }
    }

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getId());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("timer:" + Thread.currentThread().getId());
                Thread.currentThread().getParent()
            }
        },2000,4000);
    }
}
