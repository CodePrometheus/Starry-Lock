package com.star.zookeeperlock.core;

import com.star.zookeeperlock.domain.LockState;
import com.star.zookeeperlock.exception.DistributedLockException;
import com.star.zookeeperlock.util.LockCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zzStar
 * @Date: 04-20-2021 13:09
 */
@Service
public class DistributedLockFactory implements DistributedLock, DefaultDistributedLock, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(DistributedLockFactory.class);

    /**
     * 容器上下文
     */
    private ApplicationContext applicationContext;

    private ThreadLocal<DistributedLock> distributedLockThreadLocal = new ThreadLocal<>();

    private Map<String, DistributedLock> getDistributedLocks() {
        return this.applicationContext.getBeansOfType(DistributedLock.class);
    }

    private void contextExceptionNotSet() throws DistributedLockException {
        if (distributedLockThreadLocal.get() == null) {
            throw new DistributedLockException("Context exception not set");
        }
    }


    @Override
    public <T> T lock(LockContext context, LockCallBack<T> callback) {
        String namespace = context.getNamespace();
        if (!this.setLockContext(context)) {
            return null;
        }

        try {
            this.lock(namespace);
            return callback.callBack(context);
        } catch (DistributedLockException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            try {
                this.unlock(namespace);
            } catch (DistributedLockException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    @Override
    public <T> T tryLock(LockContext context, LockCallBack<T> callback) {
        String namespace = context.getNamespace();
        if (!this.setLockContext(context)) {
            return null;
        }

        try {
            if (this.tryLock(namespace)) {
                return callback.callBack(context);
            } else {
                return null;
            }
        } catch (DistributedLockException e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            try {
                this.unlock(namespace);
            } catch (DistributedLockException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean handler(LockContext lockContext) {
        return false;
    }

    @Override
    public void lock(String namespace) throws DistributedLockException {
        contextExceptionNotSet();
        this.distributedLockThreadLocal.get().lock(namespace);
    }

    @Override
    public void lockInterruptibly(String namespace) throws InterruptedException, DistributedLockException {
        contextExceptionNotSet();
        this.distributedLockThreadLocal.get().lockInterruptibly(namespace);
    }

    @Override
    public boolean tryLock(String namespace) throws DistributedLockException {
        contextExceptionNotSet();
        return this.distributedLockThreadLocal.get().tryLock(namespace);
    }

    @Override
    public boolean tryLock(String namespace, long time, TimeUnit unit) throws DistributedLockException {
        contextExceptionNotSet();
        return this.distributedLockThreadLocal.get().tryLock(namespace, time, unit);
    }

    @Override
    public void unlock(String namespace) throws DistributedLockException {
        contextExceptionNotSet();
        DistributedLock distributedLock = this.distributedLockThreadLocal.get();
        // 同时移除
        this.distributedLockThreadLocal.remove();
        distributedLock.unlock(namespace);
    }

    @Override
    public Boolean setLockContext(LockContext context) {
        Map<String, DistributedLock> distributedLockMap = getDistributedLocks();
        for (DistributedLock distributedLock : distributedLockMap.values()) {
            if (distributedLock.handler(context)) {
                Boolean setDistributedLockInvoker;
                DistributedLock lock = distributedLockThreadLocal.get();
                if (lock == null) {
                    // 未设置
                    distributedLockThreadLocal.set(distributedLock);
                    setDistributedLockInvoker = Boolean.TRUE;
                } else if (!lock.getClass().equals(distributedLock.getClass())) {
                    // 已设置 ，但是当前执行分布式锁类型与已经设置的分布式锁不一致
                    logger.error("The same thread must use the same distributed lock");
                    setDistributedLockInvoker = Boolean.FALSE;
                } else {
                    // 已设置 ，但是当前执行分布式锁类型与已经设置的分布式锁一致 ，无须重新设置
                    setDistributedLockInvoker = Boolean.TRUE;
                }
                return setDistributedLockInvoker && distributedLock.setLockContext(context);
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public LockState getLockState(String namespace) throws DistributedLockException {
        contextExceptionNotSet();
        return this.distributedLockThreadLocal.get().getLockState(namespace);
    }

    @Override
    public long getCurrentHoldThread(String namespace) throws DistributedLockException {
        contextExceptionNotSet();
        return this.distributedLockThreadLocal.get().getCurrentHoldThread(namespace);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
