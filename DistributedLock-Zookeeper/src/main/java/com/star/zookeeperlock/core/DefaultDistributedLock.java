package com.star.zookeeperlock.core;

import com.star.zookeeperlock.util.LockCallBack;

/**
 * @Author: zzStar
 * @Date: 04-20-2021 12:55
 */
public interface DefaultDistributedLock {

    <T> T lock(LockContext context, LockCallBack<T> callback);

    <T> T tryLock(LockContext context, LockCallBack<T> callback);

}
