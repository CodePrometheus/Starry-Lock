package com.star.redislock.lock;

import com.star.redislock.domain.LockInfo;
import com.star.redislock.domain.MultiLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
     * @param lockInfos
     * @return
     */
    public Lock getLock(LockInfo... lockInfos) {
        if (lockInfos.length == 1) {
            LockInfo lockInfo = lockInfos[0];
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
        } else {
            List<LockInfo> targetLockInfos = new ArrayList<>();
            for (int i = 0; i < lockInfos.length; i++) {
                targetLockInfos.add(lockInfos[i]);
            }
            return new MultiLock(redissonClient, targetLockInfos);
        }
    }
}
