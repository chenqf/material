package com.maple.user.feign;

import com.maple.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 陈其丰
 */
@FeignClient(name = "spring-cloud-alibaba-stock",path = "/stock")
public interface StockFeignService {
    @GetMapping("/num")
    Result<Integer> stock();
}
