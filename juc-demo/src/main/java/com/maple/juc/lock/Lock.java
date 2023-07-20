package com.maple.juc.lock;

import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

class Ticket{

    private final ReentrantLock reentrantLock = new ReentrantLock();
    private int number = 30;
    public synchronized void sell(){
        reentrantLock.lock();
        try{
            if(number > 0){
                System.out.println(Thread.currentThread().getName() + "::" + number-- + " 剩下:" + number);
            }
        }finally {
            reentrantLock.unlock();
        }

    }
}


/**
 * @author 陈其丰
 */
@Service
public class Lock {
    public static void main(String[] args) {
        Ticket ticket = new Ticket();
        for (int i = 0; i < 3; i++) {
            new Thread(()->{
                for (int i1 = 0; i1 < 20; i1++) {
                    ticket.sell();
                }
            },"B-child" + (i + 1)).start();
        }
    }
}
