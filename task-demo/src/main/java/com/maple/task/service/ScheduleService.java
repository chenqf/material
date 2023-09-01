package com.maple.task.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈其丰
 */
@Service
@Slf4j
public class ScheduleService {
    @Autowired
    private RedissonClient redissonClient;

    // 秒 分 时 日 月 周几
    @Scheduled(cron = "30 * * * * *") // 每分钟的第30秒
    public void ScheduleMethod() throws InterruptedException {
        RLock lock = redissonClient.getLock("myScheduledTaskLock");
        // 尝试获取锁，如果获取失败，立即返回，不会阻塞
        if (lock.tryLock()) {
            try {
                log.info(new Date() + " job1 :定时任务..");
            } finally {
                // 释放锁
                lock.unlock();
            }
        }
    }

    @Scheduled(cron = "30 * * * * *") // 每分钟的第30秒
    public void ScheduleMethod1() {
        RLock lock = redissonClient.getLock("myScheduledTaskLock");
        // 尝试获取锁，如果获取失败，立即返回，不会阻塞
        if (lock.tryLock()) {
            try {
                log.info(new Date() + " job2 :定时任务..");
            } finally {
                // 释放锁
                lock.unlock();
            }
        }
    }
}
