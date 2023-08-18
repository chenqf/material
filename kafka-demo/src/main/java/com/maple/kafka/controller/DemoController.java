package com.maple.kafka.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */
@RequestMapping("/demo")
@RestController
public class DemoController {

    @GetMapping("/test1")
    public String test1(){
        return "test1";
    }
}
