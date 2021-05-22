package com.star.redislock.domain;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 12:15
 */
public enum LockType {

    /**
     * 可重入锁
     */
    Reentrant,
    /**
     * 公平锁
     */
    Fair,
    /**
     * 读锁
     */
    Read,
    /**
     * 写锁
     */
    Write;

}
