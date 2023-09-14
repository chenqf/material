package com.maple.user.config;

import com.maple.user.interceptor.FeignAuthRequestInterceptor;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @author chenqf
 */
//@Configuration
@Slf4j
public class FeignConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignAuthRequestInterceptor();
    }

    @Bean
    public Request.Options options() {
        // connectTimeout : 连接超时时间
        // readTimeout : 连接建立后响应超时时间
        return new Request.Options(3000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS, true);
    }

    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.NONE;
    }
}
