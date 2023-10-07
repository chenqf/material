package com.maple.seata.controller;

import com.maple.seata.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 陈其丰
 */
@RestController("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/createOrder")
    public String createOrder() {
        System.out.println("order ----- createOrder");
        orderService.createOrder();
        return "success";
    }
}