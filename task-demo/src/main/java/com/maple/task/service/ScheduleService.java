package com.maple.task.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author chenqf
 */
@Service
public class ScheduleService {
    // 秒 分 时 日 月 周几
    @Scheduled(cron = "30 * * * * *") // 每分钟的第30秒
    public void ScheduleMethod(){
        System.out.println(new Date() + "  :定时任务..");
    }
}
