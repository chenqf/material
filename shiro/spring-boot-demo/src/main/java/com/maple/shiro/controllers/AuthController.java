package com.maple.shiro.controllers;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.maple.shiro.entity.User;
import com.maple.shiro.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @RequestMapping("/notLogin")
    public String error1(String name, String pwd) {
        return "notLogin";
    }

    @RequestMapping("/noRoleOrNoPermission")
    public String error2(String name, String pwd) {
        return "noRoleOrNoPermission";
    }

    @GetMapping("/login")
    @ResponseBody
    public String login(String name, String pwd, @RequestParam(defaultValue = "false") boolean rememberMe) {
        // 1. 获取subject对象
        Subject subject = SecurityUtils.getSubject();
        // 2. 封装请求数据到token
        AuthenticationToken token = new UsernamePasswordToken(name, pwd, rememberMe);
        // 3. 调用login方法进行登录认证
        try {
            subject.login(token);
            Session session = subject.getSession();
            // 缓存user信息
            User user = userService.getUserByName(name);
            subject.getSession().setAttribute("user", user);
            return (String) session.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return "登录失败";
        }
    }

    @GetMapping("logout")
    public String logout() {
        Subject subject = SecurityUtils.getSubject();
        // 登出, 清空授权缓存, 保留认证缓存
        subject.logout();
        return "logout success";
    }

    @GetMapping("changePwd")
    public String changePwd() {
//        Subject subject = SecurityUtils.getSubject();
//        subject.logout();
        return "changePwd success";
    }

}
