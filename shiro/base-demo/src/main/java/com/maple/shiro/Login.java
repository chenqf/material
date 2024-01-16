package com.maple.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;

public class Login {
    public static void main(String[] args) {
        // 1. init SecurityManager
        IniSecurityManagerFactory factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        // 2. get Subject  #subject.isPermitted("xxxx"); # subject.hasRole("xxxx")
        Subject subject = SecurityUtils.getSubject();
        // 3. get token for username and password
        AuthenticationToken token = new UsernamePasswordToken("chenqifeng", "123456");
        // 4. complete
        try {
            subject.login(token);
            System.out.println("登录成功");
            // 5. check role
            boolean hasRole = subject.hasRole("role1");
            System.out.println("hasRole role1 = " + hasRole);
            // 6. check authority
            boolean permitted = subject.isPermitted("user:insert");
            System.out.println("permitted = " + permitted);
        } catch (UnknownAccountException e) {
            e.printStackTrace();
            System.out.println("用户名不存在");
        } catch (IncorrectCredentialsException e) {
            e.printStackTrace();
            System.out.println("密码错误");
        } catch (AuthenticationException e) {
            e.printStackTrace();
            System.out.println("登录失败");
        }
    }
}
