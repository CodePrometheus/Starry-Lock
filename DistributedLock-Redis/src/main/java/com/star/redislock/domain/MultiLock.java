package com.star.redislock.domain;

import com.star.redislock.lock.Lock;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zzStar
 * @Date: 05-22-2021 19:38
 */
public class MultiLock extends Lock {

    private RedissonMultiLock rLock;

    private RedissonClient client;

    private final List<LockInfo> lockInfos;

    public MultiLock(RedissonClient client, List<LockInfo> lockInfos) {
        this.client = client;
        this.lockInfos = lockInfos;
        RLock[] rLocks = new RLock[lockInfos.size()];
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0, length = lockInfos.size(); i < length; i++) {
            name = lockInfos.get(i).getName();
            RLock lock = client.getLock(name);
            rLocks[i] = lock;
            stringBuffer.append(name);
        }
        name = stringBuffer.toString();
        this.rLock = new RedissonMultiLock(rLocks);
    }

    @Override
    public boolean acquire() {
        try {
            LockInfo lockInfo = lockInfos.get(0);
            return rLock.tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean release() {
        try {
            rLock.unlock();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
