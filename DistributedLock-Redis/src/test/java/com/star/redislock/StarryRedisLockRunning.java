package com.star.redislock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @Author: zzStar
 * @Date: 05-22-2021 15:10
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class StarryRedisLockRunning {

    public static void main(String[] args) {
        SpringApplication.run(StarryRedisLockRunning.class, args);
    }

}
