package com.maple;

/**
 * @author 陈其丰
 */
public class NewObj {
    public static void main(String[] args) {
        Customer customer = new Customer();
    }
}

class Customer{
    private long id = 100;
    private String name;
    private Account acct;
    {
        name = "匿名客户";
    }
    public Customer(){
        acct = new Account();
    }

}

class Account{}
