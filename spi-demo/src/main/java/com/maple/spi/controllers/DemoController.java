package com.maple.spi.controllers;

import com.maple.common.domain.SpiDemo1;
import com.maple.common.domain.SpiDemo2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

/**
 * @author 陈其丰
 */
@Slf4j
@RestController("/demo")
public class DemoController {

    @Autowired
    SpiDemo1 spiDemo1;

    @Autowired
    SpiDemo2 spiDemo2;

    @GetMapping("/test1")
    public String test1() {
        log.info(spiDemo1.toString());
        log.info(spiDemo2.toString());
        return "1111111";
    }
}