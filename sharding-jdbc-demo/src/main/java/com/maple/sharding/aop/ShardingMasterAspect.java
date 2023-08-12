package com.maple.sharding.aop;


import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


/**
 * @author 陈其丰
 */
@Component
@Aspect
@Slf4j
public class ShardingMasterAspect {
    @Pointcut("@annotation(com.maple.sharding.aop.ShardingMasterOnly)")
    public void cutPointcut() {}

    @Around("cutPointcut()")
    public Object masterOnly(ProceedingJoinPoint joinPoint) throws Throwable {
        HintManager instance = HintManager.getInstance();
        try{
            instance.setWriteRouteOnly();
            return joinPoint.proceed();
        }finally {
            instance.setReadwriteSplittingAuto();
            instance.close();
        }
    }
}
