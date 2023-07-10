package com.maple.user.controller;

import com.maple.common.domain.Result;
import com.maple.user.feign.StockFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqf
 */
@RequestMapping("/feign")
@RestController
public class FeignController {

    @Autowired
    private StockFeignService stockFeignService;

    @GetMapping("/stock")
    public Result demo(){
        Result<Integer> r = this.stockFeignService.stock();
        return Result.success("user:chenqf;stock:" + r.getData());
    }
}
