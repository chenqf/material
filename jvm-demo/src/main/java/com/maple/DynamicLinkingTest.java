package com.maple;

/**
 * @author 陈其丰
 */
public class DynamicLinkingTest {
    int num = 10;
    public void methodA(){
        System.out.println("methodA().....");
    }

    public void methodB(){
        System.out.println("methodB().....");

        methodA();

        num++;
    }
}
