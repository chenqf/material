package com.maple.mysql.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 陈其丰
 */
@RestController
@RequestMapping("/demo")
public class DemoController {
    @GetMapping("/test")
    public String test(){

        return "123";
    }
}
