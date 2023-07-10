package com.maple.user.feign;

import com.maple.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author chenqf
 */
@FeignClient(name = "spring-cloud-alibaba-stock",path = "/stock")
public interface StockFeignService {
    @GetMapping("/num")
    Result<Integer> stock();
}
