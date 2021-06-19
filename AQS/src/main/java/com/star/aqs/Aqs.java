package com.star.aqs;

import sun.misc.Unsafe;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

/**
 * @Author: zzStar
 * @Date: 06-19-2021 09:56
 */
public class Aqs {

    /**
     * 记录当前加锁的次数
     */
    private volatile int state = 0;

    /**
     * 当前持有锁的线程
     */
    private Thread lockHolder;

    public int getState() {
        return state;
    }

    public void setLockHolder(Thread lockHolder) {
        this.lockHolder = lockHolder;
    }


    /**
     * 线程安全的队列---基于CAS算法
     */
    private static ConcurrentLinkedQueue<Thread> waiters = new ConcurrentLinkedQueue<>();

    /**
     * 偏移量
     */
    private static final long STATEOFFSET;


    /**
     * 通过反射获取一个Unsafe实例对象
     */
    private static final Unsafe UNSAFE = UnsafeInstance.reflectGetUnsafe();

    static {
        try {
            //获取偏移量
            STATEOFFSET = UNSAFE.objectFieldOffset(Aqs.class.getDeclaredField("state"));
        } catch (Exception e) {
            throw new Error();
        }
    }

    /**
     * CAS修改 state 字段
     *
     * @param expect
     * @param update
     * @return
     */
    public final boolean compareAndSetState(int expect, int update) {
        return UNSAFE.compareAndSwapInt(this, STATEOFFSET, expect, update);
    }

    /**
     * 尝试获取锁
     *
     * @return
     */
    public boolean aquire() {
        Thread current = Thread.currentThread();
        // 当前加锁的状态
        int cnt = getState();
        if (cnt == 0) {
            // 当前同步器还没有被持有
            boolean canUse = waiters.isEmpty() || current == waiters.peek();

            if (canUse && compareAndSetState(0, 1)) {
                // 修改成功的线程，设置为持有者
                setLockHolder(current);
                return true;
            }
        }
        return false;
    }

    public void lock() {
        // 加锁成功
        if (aquire()) {
            return;
        }

        // 没有加锁成功
        Thread currentThread = Thread.currentThread();
        waiters.add(currentThread);
        // 自旋，死循环确保没有获取到锁的线程不再执行后续代码/占有CPU
        for (; ; ) {
            if (aquire()) {
                // 唤醒队头线程的话，就需要从队列中移除该线程，让后面的线程排到队首
                waiters.poll();
                return;
            }
            // 阻塞，释放CPU使用权，被刷入到运行时状态段
            LockSupport.park();
        }
    }

    public void unLock() {
        if (lockHolder != Thread.currentThread()) {
            throw new RuntimeException("lockHolder is not current thread");
        }
        int state = getState();
        if (compareAndSetState(state, 0)) {
            setLockHolder(null);
            Thread first = waiters.peek();
            if (first != null) {
                // 唤醒队首线程
                LockSupport.unpark(first);
            }
        }
    }

}
