package com.maple.user.controller;

import com.maple.common.domain.Result;
import com.maple.user.feign.StockFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenqf
 */
@RefreshScope
@RequestMapping("/nacos")
@RestController
public class NacosConfigController implements ApplicationListener<RefreshScopeRefreshedEvent> {

    @Value("${user.name}")
    private String name;

    @GetMapping("/config")
    public Result demo(){
        return Result.success(this.name);
    }
    //触发@RefreshScope执行逻辑会导致@Scheduled定时任务失效
    @Scheduled(cron = "*/3 * * * * ?") //定时任务每隔3s执行一次
    public void execute() {
        System.out.println("定时任务正常执行。。。。。。");
    }

    @Override
    public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
        this.execute();
    }
}
