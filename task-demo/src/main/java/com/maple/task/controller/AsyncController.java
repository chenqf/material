package com.maple.task.controller;

import com.maple.common.domain.Result;
import com.maple.task.service.AsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqf
 */
@RestController
public class AsyncController {

    @Autowired
    private AsyncService asyncService;

    @GetMapping("/async")
    public Result test1() throws InterruptedException {
        asyncService.test1();
        return Result.success();
    }
}
