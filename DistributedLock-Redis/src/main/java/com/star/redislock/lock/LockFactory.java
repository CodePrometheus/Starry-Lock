package com.star.redislock.lock;

import com.star.redislock.domain.LockInfo;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 13:17
 */
public class LockFactory {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 获取锁
     *
     * @param lockInfo
     * @return
     */
    public Lock getLock(LockInfo lockInfo) {
        switch (lockInfo.getType()) {
            case Reentrant:
                return new ReentrantLock(redissonClient, lockInfo);
            case Fair:
                return new FairLock(redissonClient, lockInfo);
            case Read:
                return new ReadLock(redissonClient, lockInfo);
            case Write:
                return new WriteLock(redissonClient, lockInfo);
            default:
                return new ReentrantLock(redissonClient, lockInfo);
        }
    }
}
