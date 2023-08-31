package com.maple.log.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author 陈其丰
 */
@Service
@Slf4j
public class DemoService {
    public void info(){
      log.info("我是info消息");
    }

    public void debug(){
        log.debug("我是debug消息");
    }

    public void warn(){
        log.warn("我是warn消息");
    }

    public void error(){
        try{
            int i = 1/0;
        }catch (Exception e){
            log.error(e.getMessage(),e);
            e.printStackTrace();
        }
    }
}
