package com.star.redislock.lock;

import com.star.redislock.domain.LockInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 13:23
 */
public class ReentrantLock extends Lock {

    private static final Logger logger = LoggerFactory.getLogger(ReentrantLock.class);

    private RLock rLock;

    private final LockInfo lockInfo;

    private RedissonClient redissonClient;

    public ReentrantLock(RedissonClient redissonClient, LockInfo lockInfo) {
        this.redissonClient = redissonClient;
        this.lockInfo = lockInfo;
    }

    @Override
    public boolean acquire() {
        try {
            name = lockInfo.getName();
            rLock = redissonClient.getLock(name);
            return rLock.tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean release() {
        // 判断锁是否被当前线程持有
        if (rLock.isHeldByCurrentThread()) {
            try {
                //
                return rLock.forceUnlockAsync().get();
            } catch (Exception e) {
                logger.error(e.getMessage());
                return false;
            }
        }
        return false;
    }
}
