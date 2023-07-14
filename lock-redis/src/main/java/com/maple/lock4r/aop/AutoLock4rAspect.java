package com.maple.lock4r.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author 陈其丰
 */
@Component
@Aspect
@Slf4j
public class AutoLock4rAspect {

    private final String PRE_LOCK_KEY = "Distributed-lock-redis";

    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(com.maple.lock4r.annotation.AutoLock4r)")
    public void lockPointcut() {}


    @Around("lockPointcut()")
    public Object applyLock(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Signature signature = joinPoint.getSignature();
        MethodSignature msg = (MethodSignature) signature;
        Object target = joinPoint.getTarget();
        Method method = target.getClass().getMethod(msg.getName(), msg.getParameterTypes());
        String className = target.getClass().getName();
        String methodName = method.getName();

        String lockKey = PRE_LOCK_KEY + ":" + className + ":" + methodName;
        if (args.length > 0) {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(args);
            lockKey = lockKey + ":" + json;
        }
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }
    }
}
