package com.maple.stock.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.maple.common.domain.Result;
import com.maple.stock.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqf
 */
@RequestMapping("/sentinel")
@RestController
public class SentinelController {

    @Autowired
    private TestService testService;

    @GetMapping("/flowRule")
    public Result stock(){
        return Result.success(this.testService.random());
    }
}
