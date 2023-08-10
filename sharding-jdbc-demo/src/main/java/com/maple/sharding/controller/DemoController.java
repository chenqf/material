package com.maple.sharding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
