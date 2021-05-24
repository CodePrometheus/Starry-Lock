package com.star.optimisticlock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zzStar
 * @Date: 05-10-2021 19:59
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retry {

    /**
     * 重试次数
     *
     * @return
     */
    int value() default 3;

}
