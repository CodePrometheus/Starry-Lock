package com.star.redislock;

import com.star.redislock.annotation.SLock;
import com.star.redislock.annotation.SLockKey;
import com.star.redislock.domain.LockTimeoutStrategy;
import com.star.redislock.domain.LockType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        Thread.sleep(1000 * 60);
        return "success";
    }

    @SLock(keys = {"#starry.name", "#starry.id"})
    public String getValue(Starry starry) throws InterruptedException {
        Thread.sleep(1000 * 60);
        return "success";
    }

    static List<Starry> users = new ArrayList<>();

    static {
        Starry originUser = new Starry(1, "xx", 1);
        Starry targetUser = new Starry(9, "tt", 9);
        users.add(originUser);
        users.add(targetUser);
    }

    @SLock(leaseTime = 100, waitTime = 1, lockTimeoutStrategy = LockTimeoutStrategy.FAIL_FAST, lockType = LockType.Multi)
    public String updateValue(@SLockKey Integer originUserId, @SLockKey Integer targetUserId) {
        int randNum = (int) Math.round(Math.random() * 10);
        System.out.println("thread" + Thread.currentThread().getId() + "---randNum:" + randNum);
        users.forEach(user -> {
            System.out.println(user.getSalary());
            if (Objects.equals(user.getId(), originUserId)) {
                user.setSalary(user.getSalary() - randNum);
            }
            if (Objects.equals(user.getId(), targetUserId)) {
                user.setSalary(user.getSalary() + randNum);
            }
            System.out.println("thread" + Thread.currentThread().getId() + "---" + user);
        });
        try {
//            Thread.sleep(60000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }
}
