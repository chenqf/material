package com.maple.seata.controller;

import com.maple.seata.service.XAMultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 陈其丰
 */
@RestController("/demo")
public class XAMultController {

//    @Autowired
//    private XAMultService xaMultService;
//
//    @GetMapping("/test1")
//    public String test1() {
//        xaMultService.operationMoney1();
////        xaMultService.operationMoney2();
//        return "success";
//    }
}