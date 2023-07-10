package com.maple.common.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chenqf
 */
public class CustomFeignRequestInterceptor implements RequestInterceptor {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("name","value");
        requestTemplate.query("id","11");
        logger.info("feign 拦截器!");
    }
}
