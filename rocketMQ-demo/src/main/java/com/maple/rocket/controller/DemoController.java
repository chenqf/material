package com.maple.rocket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */
@RestController
@RequestMapping("/demo")
public class DemoController {
    @GetMapping("/test1")
    public String test1(){
        return "success";
    }
}
