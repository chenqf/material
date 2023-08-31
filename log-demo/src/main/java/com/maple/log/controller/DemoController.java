package com.maple.log.controller;

import com.maple.log.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */

@RestController
public class DemoController {


    @Autowired
    private DemoService demoService;

    @GetMapping("/demo/debug")
    public String debug(){
        demoService.debug();
        return "debug";
    }

    @GetMapping("/demo/info")
    public String info(){
        demoService.info();
        return "info";
    }

    @GetMapping("/demo/warn")
    public String warn(){
        demoService.warn();
        return "warn";
    }

    @GetMapping("/demo/error")
    public String error(){
        demoService.error();
        return "error";
    }
}
