package com.star.reentrant;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author: zzStar
 * @Date: 06-19-2021 09:26
 */
public class SimpleReentrantLock {

    /**
     * 定义一个阻塞队列，当发现锁被占有的时候则进入阻塞队列
     */
    private static ConcurrentLinkedQueue<Thread> threadQueue = new ConcurrentLinkedQueue<>();

    /**
     * 定义一个静态变量，用来记录当前线程
     */
    private volatile static Thread executorThread;

    /**
     * 定义一个锁状态，根据锁状态来判断是否已被加锁
     */
    private volatile int state;

    /**
     * 是否为公平锁
     */
    private boolean isFair;

    public SimpleReentrantLock() {
        this.isFair = true;
    }

    /**
     * 加锁的方法
     */
    public void lock() {
        enqueueOrLock(Thread.currentThread());
    }

    /**
     * 入队或拿锁
     */
    public void enqueueOrLock(Thread thread) {
        /**
         * 判断state == 0， 则当前线程没有持有锁，进行加锁
         */
        if (state == 0) {
            ++state;
            executorThread = thread;
        } else if (state > 0) {
            /**
             * > 0 代表已经被加锁了，先判断进来的线程是否为当前线程，是则重入，并且++state
             * 若不是当前线程，则让当前线程进入阻塞状态
             */
            if (thread == executorThread) {
                ++state;
            } else {
                threadQueue.add(executorThread);
                // 自旋
                while (true) {
                    if (state == 0) {
                        ++state;
                        Thread next = threadQueue.poll();
                        executorThread = next;
                    }
                }
            }
        }
    }

    /**
     * 释放锁
     */
    public void unLock() {
        if (state > 0) {
            --state;
        }
    }

}
