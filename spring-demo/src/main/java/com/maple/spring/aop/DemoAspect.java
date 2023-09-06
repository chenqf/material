package com.maple.spring.aop;


import lombok.extern.slf4j.Slf4j;
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
public class DemoAspect {

    @Pointcut("@annotation(com.maple.spring.annotation.Demo)")
    public void lockPointcut() {
    }


    @Around("lockPointcut()")
    public Object applyLock(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

}
