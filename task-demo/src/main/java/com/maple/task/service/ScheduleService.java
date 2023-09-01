package com.maple.task.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author 陈其丰
 */
@Service
@Slf4j
public class ScheduleService {
    // 秒 分 时 日 月 周几
    @Scheduled(cron = "30 * * * * *") // 每分钟的第30秒
    public void ScheduleMethod() throws InterruptedException {
        log.info(new Date() + "  :定时任务..30");
        Thread.sleep(5000);
    }

    @Scheduled(cron = "33 * * * * *") // 每分钟的第30秒
    public void ScheduleMethod1() throws InterruptedException {
        log.info(new Date() + "  :定时任务..33");
        Thread.sleep(5000);
    }
}
