package com.star.optimisticlock.aspect;

import com.star.optimisticlock.annotation.Retry;
import com.star.optimisticlock.domain.Result;
import com.star.optimisticlock.exception.RetryException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * @Author: zzStar
 * @Date: 05-10-2021 20:00
 */
@Slf4j
@Aspect
@Component
public class RetryAspect {

    @Pointcut("@annotation(com.star.optimisticlock.annotation.Retry)")
    public void retryPointcut() {
    }

    @Around("retryPointcut() && @annotation(retry)")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Object tryAgain(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        int count = 0;
        do {
            count++;
            try {
                return joinPoint.proceed();
            } catch (RetryException e) {
                if (count > retry.value()) {
                    log.info("Retry Failed !");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return Result.FAILED;
                }
            }
        } while (true);
    }
}
