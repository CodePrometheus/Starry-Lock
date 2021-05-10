package com.star.zookeeperlock.core;

import com.star.zookeeperlock.domain.LockConstant;
import com.star.zookeeperlock.domain.LockState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

/**
 * @Author: zzStar
 * @Date: 04-20-2021 11:08
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ZookeeperLockContext implements LockContext {

    private String namespace;

    private String key;

    private String value;

    private long timeout;

    private long threadId;

    private LockState state;

    // zookeeper 创建的临时节点路径  公平可重入互斥锁，类似于单个JVM进程内的ReentrantLock(fair=true)
    private InterProcessMutex mutex;

    public ZookeeperLockContext(String namespace, String key, long timeout) {
        this.namespace = namespace;
        this.key = key;
        this.timeout = timeout;
    }

    public String getNamespaceAndKey() {
        return String.format("%s/%s/%s", LockConstant.ROOT_PATH, getNamespace(), getKey());
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public long getThreadId() {
        return threadId;
    }

    @Override
    public LockState getLockState() {
        return state;
    }
}
