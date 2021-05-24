package com.star.optimisticlock.service;

import com.star.optimisticlock.annotation.Retry;
import com.star.optimisticlock.domain.Account;
import com.star.optimisticlock.domain.Result;
import com.star.optimisticlock.exception.RetryException;
import com.star.optimisticlock.mapper.AccountMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.function.BiPredicate;


/**
 * @Author: zzStar
 * @Date: 05-24-2021 10:02
 */
@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Resource
    private AccountMapper accountMapper;

    private BiPredicate<BigDecimal, BigDecimal> isDepositEnough = (deposit, value) -> deposit.compareTo(value) > 0;


    /**
     * 转账 悲观锁
     *
     * @param fromId
     * @param toId
     * @param value
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Result transferPessimistic(int fromId, int toId, BigDecimal value) {
        Account from, to;

        try {
            // 先锁 id 较大的那行，避免死锁
            if (fromId > toId) {
                from = accountMapper.selectByIdForUpdate(fromId);
                to = accountMapper.selectByIdForUpdate(toId);
            } else {
                to = accountMapper.selectByIdForUpdate(toId);
                from = accountMapper.selectByIdForUpdate(fromId);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.FAILED;
        }

        /**
         * 比较金额，判断是否能进行转账
         */
        if (!isDepositEnough.test(from.getDeposit(), value)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.info(String.format("Account %d is not enough.", fromId));
            return Result.DEPOSIT_NOT_ENOUGH;
        }

        // 计算变化结果
        from.setDeposit(from.getDeposit().subtract(value));
        to.setDeposit(to.getDeposit().add(value));

        accountMapper.updateDeposit(from);
        accountMapper.updateDeposit(to);

        return Result.SUCCESS;
    }


    /**
     * 转账 乐观锁
     *
     * @param fromId
     * @param toId
     * @param value
     * @return
     */
    @Retry
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public Result transferOptimistic(int fromId, int toId, BigDecimal value) {
        Account from = accountMapper.selectById(fromId),
                to = accountMapper.selectById(toId);

        if (!isDepositEnough.test(from.getDeposit(), value)) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.DEPOSIT_NOT_ENOUGH;
        }

        from.setDeposit(from.getDeposit().subtract(value));
        to.setDeposit(to.getDeposit().add(value));

        int versionFrom, versionTo;

        if (from.getId() > to.getId()) {
            versionFrom = accountMapper.updateDepositWithVersion(from);
            versionTo = accountMapper.updateDepositWithVersion(to);
        } else {
            versionTo = accountMapper.updateDepositWithVersion(to);
            versionFrom = accountMapper.updateDepositWithVersion(from);
        }

        if (versionFrom < 1 || versionTo < 1) {
            // 失败，抛出重试异常，执行重试
            throw new RetryException("Transfer Failed, Retry.");
        } else {
            return Result.SUCCESS;
        }
    }


}
