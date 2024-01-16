package com.maple.shiro;

import org.apache.shiro.crypto.hash.Md5Hash;

public class Md5 {
    public static void main(String[] args) {
        String password = "123456";
        Md5Hash md5Hash = new Md5Hash(password);
        System.out.println("md5加密 = " + md5Hash.toHex());
        Md5Hash md5HashForSalt = new Md5Hash(password, "salt-key");
        System.out.println("md5加密 加盐 = " + md5HashForSalt.toHex());
        Md5Hash md5HashForSaltIterations = new Md5Hash(password, "salt-key", 3);
        System.out.println("md5加密 加盐 迭代3次 = " + md5HashForSaltIterations.toHex());
    }
}
