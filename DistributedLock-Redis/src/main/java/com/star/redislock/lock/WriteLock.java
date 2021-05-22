package com.star.redislock.lock;

import com.star.redislock.domain.LockInfo;
import lombok.Data;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 13:33
 */
@Data
public class WriteLock implements Lock {

    private static final Logger logger = LoggerFactory.getLogger(WriteLock.class);

    private RReadWriteLock lock;

    private final LockInfo lockInfo;

    private RedissonClient redissonClient;

    public WriteLock(RedissonClient redissonClient, LockInfo lockInfo) {
        this.redissonClient = redissonClient;
        this.lockInfo = lockInfo;
    }

    @Override
    public boolean acquire() {
        try {
            lock = redissonClient.getReadWriteLock(lockInfo.getName());
            return lock.writeLock().tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean release() {
        if (lock.writeLock().isHeldByCurrentThread()) {
            try {
                return lock.writeLock().forceUnlockAsync().get();
            } catch (Exception e) {
                logger.error(e.getMessage());
                return false;
            }
        }
        return false;
    }
}
