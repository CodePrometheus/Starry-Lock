package com.star.zookeeperlock.util;

import com.star.zookeeperlock.core.LockContext;

/**
 * 获取锁成功后回调
 *
 * @Author: zzStar
 * @Date: 04-20-2021 10:56
 */
public interface LockCallBack<T> {

    /**
     * 回调
     *
     * @param context
     * @return
     */
    T callBack(LockContext context);
}
