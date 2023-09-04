package com.maple.common.config;

import com.maple.common.domain.SpiDemo1;
import com.maple.common.domain.SpiDemo2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 陈其丰
 */
@Configuration
public class Spi2Config {
    @Bean
    public SpiDemo2 spiDemo2(){
        return new SpiDemo2(2l,"chenqf");
    }
}
