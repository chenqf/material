package com.maple.user.controller;

import com.maple.common.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author chenqf
 */
@RequestMapping("/restTemplate")
@RestController
public class RestTemplateController {

    @Autowired
    private RestTemplate template;

    @GetMapping("/stock")
    public Result demo(){
        System.out.println("尝试请求stock");
        // spring-cloud-alibaba-stock 为其他微服务在nacos中注册的应用名
        String url = "http://spring-cloud-alibaba-stock/stock/num";
        Result<Integer> r = this.template.getForObject(url, Result.class);
        return Result.success("user:chenqf;stock:" + r.getData());
    }
}
