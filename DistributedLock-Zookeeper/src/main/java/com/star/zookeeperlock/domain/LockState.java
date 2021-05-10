package com.star.zookeeperlock.domain;

/**
 * @Author: zzStar
 * @Date: 04-19-2021 23:15
 */
public enum LockState {

    /**
     * 等待
     */
    WAIT,

    /**
     * 锁定
     */
    LOCKING,

    /**
     * 释放
     */
    RELEASE
}
