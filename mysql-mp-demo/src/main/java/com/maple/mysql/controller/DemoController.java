package com.maple.mysql.controller;


import com.maple.mysql.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private DemoService demoService;

    @GetMapping("/manualTransaction")
    public String manualTransaction(){
        demoService.manualTransaction();
        return "success";
    }

    @GetMapping("/autoTransaction")
    public String autoTransaction(){
        demoService.autoTransaction();
        return "success";
    }
}
