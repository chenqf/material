package com.maple.user.controller;

import com.maple.common.domain.Result;
import com.maple.user.feign.StockFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */
@RefreshScope
@RequestMapping("/nacos")
@RestController
public class NacosConfigController {

    @Value("${user.name}")
    private String name;

    @GetMapping("/config")
    public Result demo(){
        return Result.success(this.name);
    }
}
