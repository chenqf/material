package com.maple.shiro.controllers;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @RequestMapping("/error")
    public String error(String name, String pwd) {
        return "auth error";
    }

    @GetMapping("/login")
    @ResponseBody
    public String login(String name, String pwd) {
        System.out.println("name = " + name);
        System.out.println("pwd = " + pwd);
        
        // 1. 获取subject对象
        Subject subject = SecurityUtils.getSubject();
        // 2. 封装请求数据到token
        AuthenticationToken token = new UsernamePasswordToken(name, pwd);
        // 3. 调用login方法进行登录认证
        try {
            subject.login(token);
            return "登录成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "登录失败";
        }
    }

    @GetMapping("logout")
    public String logout(String name, String pwd) {
        return "logout ------";
    }

}
