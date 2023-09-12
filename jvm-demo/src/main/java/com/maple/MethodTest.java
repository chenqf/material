package com.maple;

/**
 * @author 陈其丰
 */
public class MethodTest {

    public static void main(String[] args) {
        User user = null;
        user.hello(); // hello!
        System.out.println(user.count); // 2
        System.out.println(user.num); // 3
    }
}
class User{
    public static int count = 2;
    public final static int num = 3;

    public static void hello(){
        System.out.println("hello!");
    }
}
