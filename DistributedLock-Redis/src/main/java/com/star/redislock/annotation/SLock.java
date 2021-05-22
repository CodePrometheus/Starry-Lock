package com.star.redislock.annotation;

import com.star.redislock.domain.LockTimeoutStrategy;
import com.star.redislock.domain.LockType;
import com.star.redislock.domain.ReleaseTimeoutStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 12:13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SLock {

    /**
     * 锁名称
     *
     * @return
     */
    String name() default "";

    /**
     * 锁类型，默认可重入锁
     *
     * @return
     */
    LockType lockType() default LockType.Reentrant;

    /**
     * 获取锁等待时间
     *
     * @return
     */
    long waitTime() default Long.MIN_VALUE;

    /**
     * 自动释放锁时间
     *
     * @return
     */
    long leaseTime() default Long.MIN_VALUE;

    /**
     * 自定义业务key
     *
     * @return
     */
    String[] keys() default {};

    /**
     * 加锁超时处理策略，默认继续
     *
     * @return
     */
    LockTimeoutStrategy lockTimeoutStrategy() default LockTimeoutStrategy.GO_ON;


    /**
     * 自定义加锁超时处理策略
     *
     * @return
     */
    String customLockTimeoutStrategy() default "";

    /**
     * 释放锁时已超时的处理策略
     *
     * @return
     */
    ReleaseTimeoutStrategy releaseTimeoutStrategy() default ReleaseTimeoutStrategy.GO_ON;

    /**
     * 自定义释放锁时已超时的处理策略
     *
     * @return
     */
    String customReleaseTimeoutStrategy() default "";

}
