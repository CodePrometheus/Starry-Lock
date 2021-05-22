package com.star.redislock.lock;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 13:19
 */
public interface Lock {

    /**
     * 获取锁
     *
     * @return
     */
    boolean acquire();

    /**
     * 释放锁
     *
     * @return
     */
    boolean release();
}
