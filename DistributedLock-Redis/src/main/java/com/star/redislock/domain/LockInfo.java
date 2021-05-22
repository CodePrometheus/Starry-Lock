package com.star.redislock.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.locks.Lock;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 13:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockInfo {

    private LockType type;

    private String name;

    private long waitTime;

    private long leaseTime;


}
