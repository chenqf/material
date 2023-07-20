package com.maple.juc.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author 陈其丰
 */
public class ThreadDemo5 {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        FutureTask<Integer> futureTask = new FutureTask<>(() -> {
            System.out.println(Thread.currentThread().getName() + " come in callable");
            return 1024;
        });

        new Thread(futureTask,"t1").start();

        while (!futureTask.isDone()){
            System.out.println("wait ....");
            Thread.sleep(20);
        }
        System.out.println("result: " + futureTask.get());
        System.out.println("result: " + futureTask.get());
        System.out.println(Thread.currentThread().getName() + " come over");
    }
}
