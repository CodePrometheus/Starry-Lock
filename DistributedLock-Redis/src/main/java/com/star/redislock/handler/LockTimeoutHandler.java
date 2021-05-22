package com.star.redislock.handler;

import com.star.redislock.domain.LockInfo;
import com.star.redislock.lock.Lock;
import org.aspectj.lang.JoinPoint;

/**
 * @Author: zzStar
 * @Date: 05-22-2021 12:13
 */
public interface LockTimeoutHandler {

    /**
     * 获取锁超时处理接口
     *
     * @param lockInfo
     * @param lock
     * @param joinPoint
     */
    void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint);
}
