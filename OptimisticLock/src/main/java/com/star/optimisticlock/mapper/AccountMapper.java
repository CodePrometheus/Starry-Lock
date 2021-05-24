package com.star.optimisticlock.mapper;

import com.star.optimisticlock.domain.Account;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

/**
 * @Author: zzStar
 * @Date: 05-24-2021 10:05
 */
@Mapper
public interface AccountMapper {

    /**
     * 查找账户信息
     *
     * @param id
     * @return
     */
    Account selectById(int id);

    /**
     * 行锁
     * 当一个事务的操作未完成时候，其他事务可以读取但是不能写入或更新
     *
     * @param id
     * @return
     */
    Account selectByIdForUpdate(int id);

    /**
     * 更新version
     *
     * @param account
     * @return
     */
    int updateDepositWithVersion(Account account);

    /**
     * 更新订金
     *
     * @param account
     */
    void updateDeposit(Account account);

    /**
     * 计算订金
     *
     * @return
     */
    BigDecimal getTotalDeposit();

}
