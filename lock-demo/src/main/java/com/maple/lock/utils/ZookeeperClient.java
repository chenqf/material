package com.maple.lock.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author 陈其丰
 */
@Slf4j
@Component
public class ZookeeperClient {

    @Value("${ENV_CLOUD_IP}")
    private String host;

    private ZooKeeper zookeeper = null;

    public ZooKeeper getZookeeper() {
        return zookeeper;
    }

    @PostConstruct
    public void init() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            zookeeper = new ZooKeeper(host + ":2181", 30000,event -> {
                Watcher.Event.KeeperState state = event.getState();
                if (state.equals(Watcher.Event.KeeperState.SyncConnected)) {
                    log.info("获取到zk连接....");
                    latch.countDown();
                } else if (state.equals(Watcher.Event.KeeperState.Closed)) {
                    log.info("关闭了zk连接....");
                }
            });
            latch.await();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy(){
        try {
            this.zookeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
