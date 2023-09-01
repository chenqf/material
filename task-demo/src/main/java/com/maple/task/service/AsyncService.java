package com.maple.task.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * @author 陈其丰
 */
@Slf4j
@Service
public class AsyncService {

    @Async("asyncTask")
    public void test1(){
       log.info("test1");
    }

    @Async("asyncTask")
    public CompletableFuture<String> asyncHandler() {
        // 异步执行的任务逻辑
        String result = "Async task completed";
        int i = 1/0 ;
        return CompletableFuture.completedFuture(result);
    }
}
