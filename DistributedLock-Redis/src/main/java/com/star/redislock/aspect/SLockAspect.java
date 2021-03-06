package com.star.redislock.aspect;

import com.star.redislock.annotation.SLock;
import com.star.redislock.domain.LockInfo;
import com.star.redislock.domain.LockType;
import com.star.redislock.lock.Lock;
import com.star.redislock.lock.LockFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 19:17
 */
@Aspect
@Component
@Order(0)
public class SLockAspect {

    private static final Logger logger = LoggerFactory.getLogger(SLockAspect.class);

    @Resource
    private LockFactory lockFactory;

    @Resource
    private LockInfoProvider lockInfoProvider;

    private final Map<String, LockRes> lockThread = new ConcurrentHashMap<>();

    /**
     * try {
     * <p>
     * //@Before
     * <p>
     * result = method.invoke(target, args);
     * <p>
     * //@AfterReturning
     * <p>
     * return result;
     * <p>
     * } catch (InvocationTargetException e) {
     * <p>
     * Throwable targetException = e.getTargetException();
     * <p>
     * //@AfterThrowing
     * <p>
     * throw targetException;
     * <p>
     * } finally {
     * <p>
     * //@After
     * <p>
     * }
     */

    @Around("@annotation(sLock)")
    public Object around(ProceedingJoinPoint joinPoint, SLock sLock) throws Throwable {
        List<LockInfo> lockInfos = lockInfoProvider.get(joinPoint, sLock);
        List<String> currentLockIds = new ArrayList<>();
        lockInfos.forEach(lockInfo -> {
            String currentLockId = this.getCurrentLockId(lockInfo);
            lockThread.put(currentLockId, new LockRes(lockInfo, false));
            currentLockIds.add(currentLockId);
        });
        Lock lock = lockFactory.getLock(lockInfos.toArray(new LockInfo[]{}));
        boolean lockRes = lock.acquire();

        // ???????????????????????????????????????????????????
        if (!lockRes) {
            if (logger.isInfoEnabled()) {
                logger.warn("Timeout while acquiring Lock({})", lock.getName());
            }

            // ????????????????????????????????????????????????????????????????????????????????????
            if (!StringUtils.isEmpty(sLock.customLockTimeoutStrategy())) {
                return handleCustomLockTimeout(sLock.customLockTimeoutStrategy(), joinPoint);
            } else {
                // ??????????????????????????????????????????????????????????????????????????????????????????????????????
                lockInfos.forEach(lockInfo -> {
                    sLock.lockTimeoutStrategy().handle(lockInfo, lock, joinPoint);
                });
            }
        }

        currentLockIds.forEach(currentLockId -> {
            lockThread.get(currentLockId).setLock(lock);
            lockThread.get(currentLockId).setRes(true);
        });

        return joinPoint.proceed();
    }


    @AfterReturning("@annotation(sLock)")
    public void afterReturning(JoinPoint joinPoint, SLock sLock) {
        releaseLock(joinPoint, sLock);
    }

    @AfterThrowing(value = "@annotation(sLock)", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, SLock sLock, Throwable ex) throws Throwable {
        releaseLock(joinPoint, sLock);
        throw ex;
    }

    /**
     * ?????????
     *
     * @param joinPoint
     * @param sLock
     */
    private void releaseLock(JoinPoint joinPoint, SLock sLock) {
        try {
            List<LockInfo> lockInfos = lockInfoProvider.get(joinPoint, sLock);
            if (!CollectionUtils.isEmpty(lockInfos)) {
                if (Objects.equals(sLock.lockType(), LockType.Multi)) {
                    String currentLockId = this.getCurrentLockId(lockInfos.get(0));
                    releaseLock(sLock, joinPoint, currentLockId);
                    cleanUpThreadLocal(currentLockId);
                } else {
                    for (LockInfo lockInfo : lockInfos) {
                        String currentLock = this.getCurrentLockId(lockInfo);
                        releaseLock(sLock, joinPoint, currentLock);
                        cleanUpThreadLocal(currentLock);
                    }
                    lockInfos.forEach(lockInfo -> {
                    });
                }
            }
        } catch (Throwable tw) {
            throw new RuntimeException("release lock fail ", tw);
        }
    }


    /**
     * ????????????????????????????????????
     *
     * @param currentLockId
     */
    private void cleanUpThreadLocal(String currentLockId) {
        lockThread.remove(currentLockId);
    }

    /**
     * ?????????
     *
     * @param sLock
     * @param joinPoint
     * @param currentLockId
     */
    private void releaseLock(SLock sLock, JoinPoint joinPoint, String currentLockId) throws IllegalAccessException {
        LockRes lockRes = lockThread.get(currentLockId);
        if (Objects.isNull(lockRes)) {
            throw new NullPointerException("Please check whether the input parameter used as the lock key value has been modified in the method, " +
                    "which will cause the acquire and release locks to have different key values and throw null pointers." +
                    "currentLockId:" + currentLockId);
        }
        if (lockRes.getRes()) {
            boolean release = lockThread.get(currentLockId).getLock().release();
            // ?????????????????????????????????????????????
            lockRes.setRes(false);
            if (!release) {
                handleReleaseTimeout(sLock, lockRes.getLockInfo(), joinPoint);
            }
        }
    }


    /**
     * ?????????????????????
     *
     * @param sLock
     * @param lockInfo
     * @param joinPoint
     */
    private void handleReleaseTimeout(SLock sLock, LockInfo lockInfo, JoinPoint joinPoint) throws IllegalAccessException {
        if (logger.isWarnEnabled()) {
            logger.warn("Timeout while release Lock({})", lockInfo.getName());
        }
        if (!StringUtils.isEmpty(sLock.customReleaseTimeoutStrategy())) {
            handleCustomReleaseTimeout(sLock.customReleaseTimeoutStrategy(), joinPoint);
        } else {
            sLock.releaseTimeoutStrategy().handle(lockInfo);
        }
    }


    /**
     * ????????????????????????????????????
     *
     * @param customReleaseTimeout
     * @param joinPoint
     */
    private void handleCustomReleaseTimeout(String customReleaseTimeout, JoinPoint joinPoint) {
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(customReleaseTimeout, currentMethod.getParameterTypes());
            // ???????????????
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customReleaseTimeoutStrategy", e);
        }

        Object[] args = joinPoint.getArgs();

        try {
            handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            logger.error("Fail to invoke custom release timeout handler: " + customReleaseTimeout, e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * ???????????????????????????
     *
     * @param lockTimeoutStrategy
     * @param joinPoint
     * @return
     */
    private Object handleCustomLockTimeout(String lockTimeoutStrategy, ProceedingJoinPoint joinPoint) {
        // ?????????
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = joinPoint.getTarget().getClass().getDeclaredMethod(lockTimeoutStrategy, currentMethod.getParameterTypes());
            // ???????????????
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal annotation param customLockTimeoutStrategy", e);
        }

        Object[] args = joinPoint.getArgs();
        Object res = null;

        try {
            res = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            logger.error("Fail to invoke custom lock timeout handler: " + lockTimeoutStrategy, e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
        return res;
    }


    /**
     * ?????????????????????
     *
     * @param lockInfo
     * @return
     */
    private String getCurrentLockId(LockInfo lockInfo) {
        String currentLock = Thread.currentThread().getId() + lockInfo.getName();
        return currentLock;
    }


    private class LockRes {

        private LockInfo lockInfo;
        private Lock lock;
        private Boolean res;

        LockRes(LockInfo lockInfo, Boolean res) {
            this.lockInfo = lockInfo;
            this.res = res;
        }

        LockInfo getLockInfo() {
            return lockInfo;
        }

        public Lock getLock() {
            return lock;
        }

        public void setLock(Lock lock) {
            this.lock = lock;
        }

        Boolean getRes() {
            return res;
        }

        void setRes(Boolean res) {
            this.res = res;
        }

        void setLockInfo(LockInfo lockInfo) {
            this.lockInfo = lockInfo;
        }
    }
}
