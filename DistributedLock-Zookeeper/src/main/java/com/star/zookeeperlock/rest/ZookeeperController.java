package com.star.zookeeperlock.rest;

import com.star.zookeeperlock.core.DefaultDistributedLock;
import com.star.zookeeperlock.core.ZookeeperLockContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Author: zzStar
 * @Date: 04-20-2021 12:49
 */
@RestController
@ResponseBody
@RequiredArgsConstructor
public class ZookeeperController {

    private final DefaultDistributedLock distributedLock;

    private final Logger logger = LoggerFactory.getLogger(ZookeeperController.class);

    private Integer state = 1;


    @GetMapping("test")
    public String zkLockTest() {
        ZookeeperLockContext context = new ZookeeperLockContext("test", "1234", 3);
        Object zkLock = distributedLock.tryLock(context, c -> {
            logger.info("zk lock acquire");
            if (state > 0) {
                logger.info("state: " + (--state));
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
            return new Object();
        });
        if (zkLock != null) {
            logger.info("执行成功");
        } else {
            logger.error("执行失败");
        }
        return "200";
    }
}
