package com.maple.stock.controller;

import com.maple.common.domain.Result;
import com.maple.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqf
 */
@RequestMapping("/stock")
@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/discount")
    public Result stock() throws Exception {
        System.out.println("stock ----- discount");
        stockService.discount();
        return Result.success("success");
    }
}
