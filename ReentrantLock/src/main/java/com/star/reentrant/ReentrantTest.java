package com.star.reentrant;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: zzStar
 * @Date: 06-19-2021 09:38
 */
public class ReentrantTest {

    public static int cnt = 0;
    public static SimpleReentrantLock lock = new SimpleReentrantLock();

    public static void main(String[] args) {
        for (int i = 0; i <= 10; i++) {
            new Thread(() -> {
                for (int j = 0; j <= 10; j++) {
                    lock.lock();
                    try {
                        ++cnt;
                    } finally {
                        lock.unLock();
                    }
                }
            }).start();
        }
        System.out.println(cnt);
    }
}

class AtomicTest {
    public static AtomicInteger count = new AtomicInteger(0);
    public static Object o = new Object();

    public static void main(String[] args) {
        for (int i = 0; i <= 10; i++) {
            new Thread(() -> {
                synchronized (o) {
                    for (int j = 0; j <= 10; j++) {
                        count.addAndGet(1);
                    }
                }
            }).start();
        }
        System.out.println(count.get());
    }
}
