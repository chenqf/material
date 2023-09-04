package com.maple.common.config;

import com.maple.common.domain.SpiDemo1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author 陈其丰
 */
@Configuration
public class Spi1Config {
    @Bean
    public SpiDemo1 spiDemo1(){
        return new SpiDemo1(1l,"cqf");
    }
}
