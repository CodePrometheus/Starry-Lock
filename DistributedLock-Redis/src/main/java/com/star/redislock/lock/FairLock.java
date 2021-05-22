package com.star.redislock.lock;

import com.star.redislock.domain.LockInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 14:20
 */
public class FairLock implements Lock {

    private static final Logger logger = LoggerFactory.getLogger(FairLock.class);

    private RLock lock;

    private final LockInfo lockInfo;

    private RedissonClient redissonClient;

    public FairLock(RedissonClient redissonClient, LockInfo lockInfo) {
        this.redissonClient = redissonClient;
        this.lockInfo = lockInfo;
    }

    @Override
    public boolean acquire() {
        try {
            lock = redissonClient.getFairLock(lockInfo.getName());
            return lock.tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean release() {
        if (lock.isHeldByCurrentThread()) {
            try {
                return lock.forceUnlockAsync().get();
            } catch (Exception e) {
                logger.error(e.getMessage());
                return false;
            }
        }
        return false;
    }
}
