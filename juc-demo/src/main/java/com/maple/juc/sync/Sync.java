package com.maple.juc.sync;

import org.springframework.stereotype.Service;

class Ticket{
    private int number = 30;
    public synchronized void sell(){
        if(number > 0){
            System.out.println(Thread.currentThread().getName() + "::" + number-- + " 剩下:" + number);
        }
    }
}


/**
 * @author 陈其丰
 */
@Service
public class Sync {
    public static void main(String[] args) {
        Ticket ticket = new Ticket();
        for (int i = 0; i < 3; i++) {
            new Thread(()->{
                for (int i1 = 0; i1 < 20; i1++) {
                    ticket.sell();
                }
            },"child" + (i + 1)).start();
        }
    }
}
