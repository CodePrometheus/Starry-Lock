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

