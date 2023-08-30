package com.maple.log.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */

@RestController
public class DemoController {

    @GetMapping("/demo")
    public String demo(){
        return "success";
    }
}
