package com.maple.spring.controller;

import com.maple.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.*;

/**
 * @author 陈其丰
 */
@RestController("/demo")
@AutoConfigureAfter
@AutoConfigureBefore
@EnableConfigurationProperties
public class DemoController {

    @Autowired
    private UserService userService;

    @GetMapping("/test1")
    public String test1() {
        this.userService.test1();
        return "success";
    }
}