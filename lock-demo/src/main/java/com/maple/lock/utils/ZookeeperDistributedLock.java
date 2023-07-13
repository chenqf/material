package com.maple.lock.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * @author 陈其丰
 */
public class ZookeeperDistributedLock implements Lock {

    private ZooKeeper zookeeper;
    private String lockName;

    private String currentNodePath;

    private static final String ROOT_PATH = "/locks";

    private static final ThreadLocal<Integer> THREAD_LOCAL  = new ThreadLocal<>();

    @Override
    public void lock() {
        this.tryLock();
    }

    public ZookeeperDistributedLock(ZooKeeper zookeeper, String lockName) {
        this.zookeeper = zookeeper;
        this.lockName = lockName;
        try {
            if (zookeeper.exists(ROOT_PATH, false) == null) {
                zookeeper.create(ROOT_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    private String getPrevNode() {
        try {
            List<String> children = this.zookeeper.getChildren(ROOT_PATH, false);
            if (CollectionUtils.isEmpty(children)) {
                throw new IllegalMonitorStateException("非法操作");
            }
            List<String> list = children.stream().filter(node -> StringUtils.startsWith(node, lockName + "-")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                throw new IllegalMonitorStateException("非法操作");
            }
            Collections.sort(list);
            int i = Collections.binarySearch(list, StringUtils.substringAfterLast(currentNodePath, "/"));
            if(i < 0){
                throw new IllegalMonitorStateException("非法操作");
            }else if(i >0){
                return list.get(i -1);
            }
            return null;
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            throw new IllegalMonitorStateException("非法操作");
        }
    }

    @Override
    public boolean tryLock() {
        Integer flag = THREAD_LOCAL.get();
        if (flag != null && flag > 0) {
            THREAD_LOCAL.set(flag +1);
            return true;
        }
        try {
            currentNodePath = this.zookeeper.create(ROOT_PATH + "/" + this.lockName + "-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            String prevNode = this.getPrevNode();
            if (prevNode != null) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                if(this.zookeeper.exists(ROOT_PATH + "/" + prevNode, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        countDownLatch.countDown();
                    }
                }) == null){
                    THREAD_LOCAL.set(1);
                    return true;
                }
                countDownLatch.await();
            }
            THREAD_LOCAL.set(1);
            return true;
        } catch (KeeperException | InterruptedException e) {
            try {
                TimeUnit.MICROSECONDS.sleep(50);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            this.tryLock();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, @NotNull TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        try {
            THREAD_LOCAL.set(THREAD_LOCAL.get() - 1);
            if(THREAD_LOCAL.get() == 0){
                this.zookeeper.delete(currentNodePath, -1);
            }
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public Condition newCondition() {
        return null;
    }
}
