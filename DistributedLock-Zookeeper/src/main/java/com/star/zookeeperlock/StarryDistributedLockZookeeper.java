package com.star.zookeeperlock;

import com.star.zookeeperlock.util.EnableDistributedLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: zzStar
 * @Date: 04-19-2021 22:44
 */
@EnableDistributedLock
@SpringBootApplication
public class StarryDistributedLockZookeeper {

    public static void main(String[] args) {
        SpringApplication.run(StarryDistributedLockZookeeper.class, args);
    }

}
