package com.star.redislock.lock;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 13:19
 */
public class Lock {

    public String name = "lock";

    /**
     * 获取锁
     *
     * @return
     */
    public boolean acquire() {
        return true;
    }

    /**
     * 释放锁
     *
     * @return
     */
    public boolean release() {
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
