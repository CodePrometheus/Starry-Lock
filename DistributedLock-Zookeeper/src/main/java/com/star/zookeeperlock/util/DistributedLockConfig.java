package com.star.zookeeperlock.util;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = DistributedLockConfig.class)
public class DistributedLockConfig {
}
