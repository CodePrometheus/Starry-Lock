package com.star.zookeeperlock.core;

import com.star.zookeeperlock.domain.LockState;

/**
 * 锁的上下文
 *
 * @Author: zzStar
 * @Date: 04-19-2021 23:17
 */
public interface LockContext {

    /**
     * 命名
     *
     * @return
     */
    String getNamespace();

    /**
     * key
     *
     * @return
     */
    String getKey();

    /**
     * Value
     *
     * @return
     */
    String getValue();

    /**
     * 超时时间
     *
     * @return
     */
    long getTimeout();


    /**
     * 线程id
     *
     * @return
     */
    long getThreadId();

    /**
     * 锁的状态
     *
     * @return
     */
    LockState getLockState();
}
