package com.star.redislock;

import com.star.redislock.annotation.SLock;
import com.star.redislock.annotation.SLockKey;
import com.star.redislock.domain.LockTimeoutStrategy;
import org.springframework.stereotype.Service;

/**
 * @Author: zzStar
 * @Date: 05-22-2021 15:36
 */
@Service
public class StarryService {

    @SLock(waitTime = 10,
            leaseTime = 60,
            keys = {"#param"},
            lockTimeoutStrategy = LockTimeoutStrategy.FAIL_FAST)
    public String getValue(String param) throws InterruptedException {
        if ("sleep".equals(param)) {
            Thread.sleep(1000 * 3);
        }
        return "success";
    }

    @SLock(keys = {"#userId"})
    public String getValue(String userId, @SLockKey Integer id) throws InterruptedException {
        Thread.sleep(1000 * 50);
        return "success";
    }

    @SLock(keys = {"#starry.name", "#starry.id"})
    public String getValue(Starry starry) throws InterruptedException {
        Thread.sleep(1000 * 50);
        return "success";
    }
}
