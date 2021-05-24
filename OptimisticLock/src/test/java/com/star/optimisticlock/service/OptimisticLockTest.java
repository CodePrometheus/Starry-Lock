package com.star.optimisticlock.service;

import com.star.optimisticlock.domain.Result;
import com.star.optimisticlock.mapper.AccountMapper;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static java.lang.System.currentTimeMillis;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
class AccountServiceTest {

    // 并发数
    private static final int COUNT = 500;

    @Resource
    AccountMapper accountMapper;

    @Resource
    AccountService accountService;

    private CountDownLatch latch = new CountDownLatch(COUNT);
    private List<Thread> transferThreads = new ArrayList<>();
    private List<Pair<Integer, Integer>> transferAccounts = new ArrayList<>();

    @Test
    public void total() {
        System.out.println(accountMapper.getTotalDeposit());
    }

    @BeforeEach
    void setUp() {
        Random random = new Random(currentTimeMillis());
        transferThreads.clear();
        transferAccounts.clear();

        for (int i = 0; i < COUNT; i++) {
            int from = random.nextInt(10) + 1;
            int to;
            do {
                to = random.nextInt(10) + 1;
            } while (from == to);
            transferAccounts.add(new Pair<>(from, to));
        }
    }


    /**
     * 测试悲观锁
     */
    @Test
    void transferByPessimisticLock() throws Throwable {
        for (int i = 0; i < COUNT; i++) {
            transferThreads.add(new Transfer(i, true));
        }
        for (Thread t : transferThreads) {
            t.start();
        }
        latch.await();

        Assertions.assertEquals(accountMapper.getTotalDeposit(),
                BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 测试乐观锁
     */
    @Test
    void transferByOptimisticLock() throws Throwable {
        for (int i = 0; i < COUNT; i++) {
            transferThreads.add(new Transfer(i, false));
        }
        for (Thread t : transferThreads) {
            t.start();
        }
        latch.await();

        Assertions.assertEquals(accountMapper.getTotalDeposit(),
                BigDecimal.valueOf(1000).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 转账线程
     */
    class Transfer extends Thread {
        int index;
        boolean isPessimistic;

        Transfer(int i, boolean b) {
            index = i;
            isPessimistic = b;
        }

        @Override
        public void run() {
            BigDecimal value = BigDecimal.valueOf(
                    new Random(currentTimeMillis()).nextFloat() * 5
            ).setScale(2, RoundingMode.HALF_UP);

            Result result = Result.FAILED;
            int fromId = transferAccounts.get(index).getKey(),
                    toId = transferAccounts.get(index).getValue();
            try {
                if (isPessimistic) {
                    result = accountService.transferPessimistic(fromId, toId, value);
                } else {
                    result = accountService.transferOptimistic(fromId, toId, value);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                if (result == Result.SUCCESS) {
                    log.info(String.format("Transfer %f from %d to %d success", value, fromId, toId));
                }
                latch.countDown();
            }
        }
    }
}
