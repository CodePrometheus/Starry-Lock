package com.star.zookeeperlock.core;

import com.star.zookeeperlock.domain.LockState;
import com.star.zookeeperlock.exception.DistributedLockException;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁核心接口
 *
 * @Author: zzStar
 * @Date: 04-20-2021 10:43
 */
public interface DistributedLock {

    /**
     * 是否能进行处理
     *
     * @param lockContext
     * @return
     */
    boolean handler(LockContext lockContext);

    /**
     * Acquires the lock.
     *
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until the
     * lock has been acquired.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>A {@code Lock} implementation may be able to detect erroneous use
     * of the lock, such as an invocation that would cause deadlock, and
     * may throw an (unchecked) exception in such circumstances.  The
     * circumstances and the exception type must be documented by that
     * {@code Lock} implementation.
     *
     * @param namespace
     */
    void lock(String namespace) throws DistributedLockException;


    /**
     * 优先考虑响应中断，而不是响应锁的普通获取或重入获取
     *
     * @param namespace
     * @throws InterruptedException
     * @throws DistributedLockException
     */
    void lockInterruptibly(String namespace) throws InterruptedException, DistributedLockException;


    /**
     * 获取锁
     *
     * @param namespace
     * @return
     * @throws DistributedLockException
     */
    boolean tryLock(String namespace) throws DistributedLockException;

    /**
     * 获取锁定义超时时间
     *
     * @param namespace
     * @param time
     * @param unit
     * @return
     * @throws DistributedLockException
     */
    boolean tryLock(String namespace, long time, TimeUnit unit) throws DistributedLockException;


    /**
     * 释放锁
     *
     * @param namespace
     * @throws DistributedLockException
     */
    void unlock(String namespace) throws DistributedLockException;

    /**
     * 是否设置上下文
     *
     * @param context
     * @return
     */
    Boolean setLockContext(LockContext context);

    /**
     * 获取锁的状态
     *
     * @param namespace
     * @return
     * @throws DistributedLockException
     */
    LockState getLockState(String namespace) throws DistributedLockException;

    /**
     * 获取锁的当前持有线程id
     *
     * @param namespace
     * @return
     * @throws DistributedLockException
     */
    long getCurrentHoldThread(String namespace) throws DistributedLockException;
}
