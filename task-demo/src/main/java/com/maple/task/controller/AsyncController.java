package com.maple.task.controller;

import com.maple.task.service.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * @author 陈其丰
 */
@Slf4j
@RestController("/demo")
public class AsyncController {

    @Autowired
    private AsyncService asyncService;

    @GetMapping("/test1")
    public String test1() {
        try {
            CompletableFuture<String> asyncResult = asyncService.asyncHandler();
            String result = asyncResult.join(); // 阻塞等待异步任务完成并获取返回值
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
}