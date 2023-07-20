package com.maple.juc.thread;

import java.util.concurrent.CountDownLatch;

/**
 * @author 陈其丰
 */
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            new Thread(()->{
                System.out.println("离开");
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        System.out.println("锁门");
    }
}
