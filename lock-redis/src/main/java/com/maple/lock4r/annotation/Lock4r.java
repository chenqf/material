package com.maple.lock4r.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author 陈其丰
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock4r {
    @AliasFor("key")
    String value() default "";

    @AliasFor("value")
    String key() default "";

    long waitTime() default -1L;

    long expireTime() default -1L;
}
