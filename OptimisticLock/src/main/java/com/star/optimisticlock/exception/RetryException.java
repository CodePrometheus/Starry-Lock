package com.star.optimisticlock.exception;

/**
 * @Author: zzStar
 * @Date: 05-10-2021 20:08
 */
public class RetryException extends RuntimeException{

    public RetryException(String msg) {
        super(msg);
    }
}
