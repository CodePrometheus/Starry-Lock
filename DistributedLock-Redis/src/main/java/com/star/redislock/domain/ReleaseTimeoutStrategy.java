package com.star.redislock.domain;

import com.star.redislock.handler.ReleaseTimeoutHandler;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 13:06
 */
public enum ReleaseTimeoutStrategy implements ReleaseTimeoutHandler {

    /**
     * 继续执行业务逻辑，不做任何处理
     */
    GO_ON() {
        @Override
        public void handle(LockInfo lockInfo) {
        }
    },

    /**
     * 快速失败
     */
    FAIL_FAST() {
        @Override
        public void handle(LockInfo lockInfo) {

            String errorMsg = String.format("Found Lock(%s) already been released while lock lease time is %d s", lockInfo.getName(), lockInfo.getLeaseTime());
            throw new RuntimeException(errorMsg);
        }
    }
}
