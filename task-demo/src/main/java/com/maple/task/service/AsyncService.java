package com.maple.task.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author chenqf
 */
@Service
public class AsyncService {
    @Async
    public void test1() throws InterruptedException {
        System.out.println("数据处理中...");
        Thread.sleep(3000);
        System.out.println("数据处理完成...");
    }
}
