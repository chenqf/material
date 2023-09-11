package com.maple.stock.controller;

import com.maple.common.domain.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqf
 */
@RequestMapping("/stock")
@RestController
public class StockController {

    @GetMapping("/num")
    public Result stock(){
        System.out.println("--------- Stock");
        return Result.success(10);
    }
}
