package com.star.redislock.handler;

import com.star.redislock.domain.LockInfo;

/**
 * @Author: zzStar
 * @Date: 05-22-2021 12:12
 */
public interface ReleaseTimeoutHandler {

    /**
     * 释放锁超时接口
     *
     * @param lockInfo
     */
    void handle(LockInfo lockInfo);

}
