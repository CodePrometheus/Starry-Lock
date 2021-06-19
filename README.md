# Starry-Lock
分布式锁，乐观锁，悲观锁，可重入锁，非可重入锁，互斥锁，读写锁，分段锁，类锁，行级锁，自旋锁，偏向锁，公平锁，非公平锁共享锁，排他锁


## Zookeeper分布式锁

- [Zookeeper分布式锁](https://codeprometheus.github.io/Starry-Notes/#/Concurrency/Zookeeper%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81)

## Redis分布式锁

![锁超时处理逻辑](https://wx1.sinaimg.cn/large/7dfa0a7bly1g24obim6cnj20u80jzgnf.jpg "锁超时处理逻辑.jpg")


## 悲观锁和乐观锁

- [悲观锁和乐观锁](https://codeprometheus.github.io/Starry-Notes/#/Concurrency/%E6%82%B2%E8%A7%82%E9%94%81%E5%92%8C%E4%B9%90%E8%A7%82%E9%94%81)


## AQS

如果被请求的共享资源空闲，那么就将当前请求资源的线程设置为有效的工作线程，将共享资源设置为锁定状态；如果共享资源被占用，就需要一定的阻塞等待唤醒机制来保证锁分配。这个机制主要用的是CLH队列的变体实现的，将暂时获取不到锁的线程加入到队列中。

-[AQS](https://codeprometheus.github.io/Starry-Notes/#/Concurrency/AQS)

## ReentrantLock

可重入锁，也叫做递归锁，是指在同一个线程在调外层方法获取锁的时候，再进入内层方法会自动获取锁。ReentrantLock 和synchronized 都是 可重入锁。可重入锁的一个好处是可一定程度避免死锁。

-[ReentrantLock](https://codeprometheus.github.io/Starry-Notes/#/Concurrency/ReentrantLock)


## 公平锁

公平锁是指多个线程按照申请锁的顺序来获取锁

-[公平锁](https://codeprometheus.github.io/Starry-Notes/#/Concurrency/AQS)


## 非公平锁

非公平锁是指多个线程获取锁的顺序并不是按照申请锁的顺序，有可能后申请的线程比先申请的线程优先获取锁。

可能造成优先级反转或者饥饿现象。对于Java ReentrantLock而言，通过构造函数 ReentrantLock(boolean fair) 指定该锁是否是公平锁，默认是非公平锁。

非公平锁的优点在于吞吐量比公平锁大。对于Synchronized而言，也是一种非公平锁。

-[非公平锁](https://codeprometheus.github.io/Starry-Notes/#/Concurrency/AQS)


## 分段锁

分段锁其实是一种锁的设计，目的是细化锁的粒度，并不是具体的一种锁，对于ConcurrentHashMap而言，其并发的实现就是通过分段锁的形式来实现高效的并发操作。

ConcurrentHashMap中的分段锁称为Segment，它即类似于HashMap（JDK7 中HashMap的实现）的结构，即内部拥有一个Entry数组，数组中的每个元素又是一个链表；同时又是一个ReentrantLock（Segment继承了ReentrantLock)。

当需要put元素的时候，并不是对整个HashMap加锁，而是先通过hashcode知道要放在哪一个分段中，然后对这个分段加锁，所以当多线程put时，只要不是放在同一个分段中，可支持并行插入。

-[分段锁](https://codeprometheus.github.io/Starry-Notes/#/KeyPoints/ConcurrentHashMap1.6&1.7)
