package com.maple.juc.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

/**
 * @author 陈其丰
 */

class Share{
    private int num = 0;

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void increase() throws InterruptedException {
        lock.lock();
        try{
            while (num != 0){
                condition.await();
            }
            num++;
            System.out.println(Thread.currentThread().getName() + "::" + num);
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public void decrease() throws InterruptedException {
        lock.lock();
        try{
            while (num != 1){
                condition.await();
            }
            num--;
            System.out.println(Thread.currentThread().getName() + "::" + num);
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }
}

public class ThreadDemo2 {
    public static void main(String[] args) {
        Share share = new Share();
        for (int i = 0; i < 2; i++) {
            new Thread(()->{
                for (int i1 = 0; i1 < 10; i1++) {
                    try {
                        share.increase();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            },"in-" + i).start();
            new Thread(()->{
                for (int i1 = 0; i1 < 10; i1++) {
                    try {
                        share.decrease();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            },"de-" + i).start();
        }
    }
}
