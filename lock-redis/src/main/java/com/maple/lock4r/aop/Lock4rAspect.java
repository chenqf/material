package com.maple.lock4r.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maple.lock4r.annotation.Lock4r;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈其丰
 */
@Component
@Aspect
@Slf4j
public class Lock4rAspect {

    private final String PRE_LOCK_KEY = "Distributed-Lock";

    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(com.maple.lock4r.annotation.Lock4r)")
    public void lockPointcut() {
    }


    @Around("lockPointcut()")
    public Object applyLock(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        MethodSignature msg = (MethodSignature) joinPoint.getSignature();
        Object target = joinPoint.getTarget();
        Method method = target.getClass().getMethod(msg.getName(), msg.getParameterTypes());
        String className = target.getClass().getName();
        String methodName = method.getName();
        String lockKey = "";

        Lock4r annotation = method.getAnnotation(Lock4r.class);
        String value = annotation.value();
        long waitTime = annotation.waitTime();
        long expireTime = annotation.expireTime();

        if (value.isEmpty()) {
            if (args.length > 0) {
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(args);
                lockKey = PRE_LOCK_KEY + ":" + className + ":" + methodName + ":" + json;
            } else {
                lockKey = PRE_LOCK_KEY + ":" + className + ":" + methodName + ":[]";
            }
        } else if (isSpel(value)) {
            lockKey = PRE_LOCK_KEY + ":SPEL:" + getBySpel(value,joinPoint);
        }else{
            lockKey = PRE_LOCK_KEY + ":SIMPLE:" + value;
        }

        RLock lock = redissonClient.getLock(lockKey);
        boolean result = true;
        if (waitTime > 0 && expireTime > 0) {
            result = lock.tryLock(waitTime, expireTime, TimeUnit.SECONDS);
        } else if (waitTime > 0) {
            result = lock.tryLock(waitTime, TimeUnit.SECONDS);
        } else if (expireTime > 0) {
            lock.lock(expireTime, TimeUnit.SECONDS);
        } else {
            lock.lock();
        }

        if (!result) {
            throw new IllegalStateException("连接超时,请稍后重试");
        }

        try {
            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取方法参数名两数组
     */
    private String[] getParameterNames(Method method) {
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        return u.getParameterNames(method);
    }

    /**
     * 解析SPEL
     */
    private String getBySpel(String spel, ProceedingJoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String[] paramNames = getParameterNames(method);
        Object[] arguments = point.getArgs();

        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(spel);
        EvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < arguments.length; i++) {
            context.setVariable(paramNames[i], arguments[i]);
        }

        return expression.getValue(context, String.class);
    }

    private boolean isSpel(String str) {
        return str.contains("#");
    }
}
