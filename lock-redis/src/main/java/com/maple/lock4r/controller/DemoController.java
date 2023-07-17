package com.maple.lock4r.controller;

import com.maple.lock4r.Service.DemoService;
import com.maple.lock4r.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("test1")
    public String demo1() throws InterruptedException {
        this.demoService.test1();
        return "----";
    }

    @GetMapping("test2")
    public String demo2() throws InterruptedException {
        this.demoService.test2(new Book(1,"chenqf"));
        return "----";
    }
}
