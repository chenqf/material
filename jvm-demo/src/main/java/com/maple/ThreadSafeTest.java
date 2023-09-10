package com.maple;

/**
 * @author 陈其丰
 */
public class ThreadSafeTest {

    // 线程安全
    public static void method1(){
        StringBuilder builder = new StringBuilder();
        builder.append("s");
        builder.append("s");
    }


    // 线程不安全
    public static void method2(StringBuilder builder){
        builder.append("s");
        builder.append("s");
    }

    // 线程不安全
    public static StringBuilder method3(){
        StringBuilder builder = new StringBuilder();
        builder.append("s");
        builder.append("s");
        return builder;
    }
}
