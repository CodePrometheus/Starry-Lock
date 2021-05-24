package com.star.optimisticlock.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: zzStar
 * @Date: 05-10-2021 19:47
 */
@Data
public class Account {

    private int id;

    private BigDecimal deposit;

    private int version;

}
