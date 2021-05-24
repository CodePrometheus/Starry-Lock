package com.star.optimisticlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author: zzStar
 * @Date: 05-24-2021 10:50
 */
@SpringBootApplication
@EnableTransactionManagement
public class StarryOptimisticLockRunning {

    public static void main(String[] args) {
        SpringApplication.run(StarryOptimisticLockRunning.class, args);
    }

}
