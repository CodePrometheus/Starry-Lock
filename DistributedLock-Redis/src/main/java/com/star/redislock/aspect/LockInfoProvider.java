package com.star.redislock.aspect;

import com.star.redislock.annotation.SLock;
import com.star.redislock.config.LockConfig;
import com.star.redislock.domain.LockInfo;
import com.star.redislock.domain.LockType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 19:19
 */
public class LockInfoProvider {

    @Resource
    private LockConfig lockConfig;

    @Resource
    private BusinessKey businessKey;

    private static final Logger logger = LoggerFactory.getLogger(LockInfoProvider.class);

    private static final String LOCK_NAME_PREFIX = "lock";

    private static final String LOCK_NAME_SEPARATOR = ".";

    /**
     * @param joinPoint
     * @param sLock
     * @return
     */
    public LockInfo get(JoinPoint joinPoint, SLock sLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LockType lockType = sLock.lockType();
        String keyName = businessKey.getKeyName(joinPoint, sLock);
        // 控制锁的粒度
        String lockName = LOCK_NAME_PREFIX + LOCK_NAME_SEPARATOR + getName(sLock.name(), signature) + keyName;

        long waitTime = getWaitTime(sLock);
        long leaseTime = getLeaseTime(sLock);
        if (leaseTime == -1 && logger.isInfoEnabled()) {
            logger.warn("Trying to acquire Lock({}) with no expiration, " +
                    "SLock will keep prolong the lock expiration while the lock is still holding by current thread. " +
                    "This may cause dead lock in some circumstances.", lockName);
        }
        return new LockInfo(lockType, lockName, waitTime, leaseTime);
    }


    /**
     * 获取锁的name，没有指定则按全类名拼接方法名处理
     *
     * @param name
     * @param signature
     * @return
     */
    private String getName(String name, MethodSignature signature) {
        if (name.isEmpty()) {
            return String.format("%s.%s", signature.getDeclaringTypeName(), signature.getMethod().getName());
        } else {
            return name;
        }
    }

    private long getWaitTime(SLock sLock) {
        return sLock.waitTime() == Long.MIN_VALUE ?
                lockConfig.getWaitTime() : sLock.waitTime();
    }

    private long getLeaseTime(SLock sLock) {
        return sLock.leaseTime() == Long.MIN_VALUE ?
                lockConfig.getLeaseTime() : sLock.leaseTime();
    }

}
