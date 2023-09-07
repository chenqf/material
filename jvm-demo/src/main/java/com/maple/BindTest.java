package com.maple;

/**
 * @author 陈其丰
 */
public class BindTest {

    interface Huntable{
        void hunt();
    }

    class Animal{
        public void eat(){

        }

        public void eat(String anme){

        }
    }

    class Cat extends Animal implements Huntable{

        public Cat() {
            super(); // 早期绑定
        }
        public Cat(String name){
            this(); // 早期绑定
        }

        @Override
        public void hunt() {

        }

        @Override
        public void eat() {

        }
    }

    public void showAnimal(Animal animal){
        animal.eat(); // 晚期绑定
    }

    public void showHunt(Huntable h){
        h.hunt(); // 晚期绑定
    }

}
