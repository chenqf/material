package com.maple.cache.controller;

import com.maple.cache.service.DemoService;
import com.maple.common.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private DemoService demoService;


    @RequestMapping("/test4")
    public Result test4(){

        String s1 = demoService.testCache1();
        String s2 = demoService.testCache2();
        String s3 = demoService.testCache3();

        System.out.println("s1:" + s1);
        System.out.println("s2:" + s2);
        System.out.println("s3:" + s3);


        return Result.success("");
    }
}
