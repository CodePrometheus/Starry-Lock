package com.star.redislock;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @Author: zzStar
 * @Date: 05-22-2021 15:49
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = StarryRedisLockRunning.class)
public class SLockTest {

    private static final Logger logger = LoggerFactory.getLogger(SLockTest.class);


    @Resource
    private StarryService starryService;

    @Resource
    private TimeoutService timeoutService;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * 同一进程内多线程获取锁测试
     */
    @Test
    public void multithreadingTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        IntStream.range(0, 10).forEach(i -> executorService.submit(() -> {
            try {
                String res = starryService.getValue("sleep");
                logger.info("线程:[ " + Thread.currentThread().getName() + " ]拿到结果 => " + res + new Date().toLocaleString());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }));
        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    public void multiLockTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        IntStream.range(0, 10).forEach(i -> executorService.submit(() -> {
            String res = starryService.updateValue(1, 9);
            System.out.println(res);
        }));
        executorService.awaitTermination(60 * 5, TimeUnit.SECONDS);
    }

    /**
     * 线程休眠
     */
    @Test
    public void sleep() throws InterruptedException {
        String res = starryService.getValue("sleep");
        Assert.assertEquals(res, "success");
    }

    /**
     * 先后启动 sleep 和 noSleep 两个测试用例，会发现虽然 noSleep 没休眠,因为getValue加锁了，
     * 所以只要 sleep 拿到锁就基本同时完成
     *
     * @throws InterruptedException
     */
    @Test
    public void noSleep() throws InterruptedException {
        String res = starryService.getValue("noSleep");
        Assert.assertEquals(res, "success");
    }

    @Test
    public void keyTest1() throws InterruptedException {
        String res = starryService.getValue("user1", null);
        Assert.assertEquals(res, "success");
    }

    @Test
    public void keyTest2() throws InterruptedException {
        String res = starryService.getValue("user1", 1);
        Assert.assertEquals(res, "success");
    }

    @Test
    public void keyTest3() throws InterruptedException {
        String res = starryService.getValue("user1", 2);
        Assert.assertEquals(res, "success");
    }

    @Test
    public void keyTest4() throws InterruptedException {
        String res = starryService.getValue(new Starry(3, null, 0));
        Assert.assertEquals(res, "success");
    }

    /**
     * 测试watchdog无限延长加锁时间
     */
    @Test
    public void infiniteLeaseTime() {
        timeoutService.foo1();
    }

    /**
     * 测试加锁超时快速失败
     */
    @Test
    public void lockTimeoutFailFast() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.submit(() -> timeoutService.foo1());
        TimeUnit.MILLISECONDS.sleep(1000);
        logger.info("1s后");
        timeoutService.foo2();
    }

    /**
     * 测试加锁超时阻塞等待
     * 会打印10次foo3 acquire lock
     */
    @Test
    public void lockTimeoutKeepAcquire() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    timeoutService.foo3();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        long start = System.currentTimeMillis();
        startLatch.countDown();
        endLatch.await();
        long end = System.currentTimeMillis();
        Assert.assertTrue((end - start) >= 10 * 2 * 1000);
    }

    /**
     * 测试自定义加锁超时处理策略
     * 会执行1次自定义加锁超时处理策略
     */
    @Test
    public void lockTimeoutCustom() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(2);

        executorService.submit(() -> {
            timeoutService.foo1();
            latch.countDown();
        });

        executorService.submit(() -> {
            timeoutService.foo4("foo", "bar");
            latch.countDown();
        });
        latch.await();
    }

    /**
     * 测试加锁超时不做处理
     */
    @Test
    public void lockTimeoutNoOperation() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    timeoutService.foo5("foo", "bar");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });

        }

        long start = System.currentTimeMillis();
        startLatch.countDown();
        endLatch.await();
        long end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 10 * 2 * 1000);
    }

    /**
     * 测试释放锁时已超时，不做处理
     */
    @Test
    public void releaseTimeoutNoOperation() {
        timeoutService.foo6("foo", "bar");
    }

    /**
     * 测试释放锁时已超时，快速失败
     */
    @Test
    public void releaseTimeoutFailFast() {
        exception.expect(RuntimeException.class);
        timeoutService.foo7("foo", "bar");
    }

    /**
     * 测试释放锁时已超时，自定义策略
     */
    @Test
    public void releaseTimeoutCustom() {
        exception.expect(IllegalStateException.class);
        timeoutService.foo8("foo", "bar");
    }

}
