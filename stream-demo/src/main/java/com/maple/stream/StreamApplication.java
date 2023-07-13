package com.maple.stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author 陈其丰
 */
public class StreamApplication {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Apple {
        private Integer id;
        private String color;
        private Integer weight;
        private String origin;
    }

    public static List<Apple> appleList = new ArrayList<>();

    static {
        appleList.add(new Apple(1, "red", 500, "湖南"));
        appleList.add(new Apple(2, "red", 400, "北京"));
        appleList.add(new Apple(3, "green", 300, "北京"));
        appleList.add(new Apple(4, "green", 200, "上海"));
        appleList.add(new Apple(5, "green", 100, "上海"));
        appleList.add(new Apple(6, "yellow", 200, "大连"));
    }

    public static void main(String[] args) {
        StreamApplication app = new StreamApplication();

//        app.test1();
//        app.test2();
//        app.test3(apple -> apple.getColor().equals("red"));
        app.test4();
    }

    public void test1() {
        List<Apple> apples = appleList.stream().filter(apple -> apple.getColor().equals("red"))
                .collect(Collectors.toList());
        System.out.println(apples);
    }

    public void test2(){
        List<Apple> apples = appleList.stream()
                .filter(apple -> apple.getColor().equals("red"))
                .filter(apple -> apple.getWeight()>300)
                .collect(Collectors.toList());
        System.out.println(apples);
    }

    public void test3(Predicate<? super Apple> pr){
        List<Apple> apples = appleList.stream()
                .filter(pr)
                .collect(Collectors.toList());
        System.out.println(apples);
    }

    public void test4(){
        // 求每个颜色的平均重量
//        Map<String, List<Apple>> collect = appleList.stream()
//                .collect(Collectors.groupingBy(a -> a.getColor()));
        Map<String, Double> collect1 = appleList.stream()
                .collect(Collectors.groupingBy(a -> a.getColor(), Collectors.averagingInt(a -> a.getWeight())));
        System.out.println(collect1);
    }

    public void test5(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("匿名内部类");
            }
        }).start();
        // 1. 只能有一个方法的情况下，可以这么写 （函数式接口）
        // 2. 默认方法除外
        new Thread(() -> System.out.println("匿名内部类")).start();
    }
    public void test6(){
        test(new Test() {
            @Override
            public void aaa() {

            }
        });
    }
    public static void test(Test t){
        t.aaa();
    }

    public interface Test{
        void aaa();
        default void bbb(){}
    }
}
