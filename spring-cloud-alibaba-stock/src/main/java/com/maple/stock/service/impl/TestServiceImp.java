package com.maple.stock.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.maple.stock.service.TestService;
import jdk.nashorn.internal.ir.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author chenqf
 */
@Service
public class TestServiceImp implements TestService {

    private  final Logger logger = LoggerFactory.getLogger(TestServiceImp.class);

    @Override
    @SentinelResource(value = "random",blockHandler = "randomBlockExceptionHandler")
    public Integer random() {
        return (int)Math.round(Math.random() * 10);
    }

    public Integer randomBlockExceptionHandler(BlockException e) throws BlockException {
        logger.info("BlockExceptionHandler BlockException===========" + e.getRule());
        return null;
    }
}
