package com.maple.shiro.controllers;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maple.shiro.entity.User;
import com.maple.shiro.service.UserService;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private UserService userService;


    /**
     * 登录才能访问
     */
    @GetMapping("/demo1")
    public String demo1() {
        return "success";
    }

    /**
     * 登录才能访问
     */
    @GetMapping("/getUserByName/{name}")
    public User getUserByName(@PathVariable String name) {
        return userService.getUserByName(name);
    }

    /**
     * 登录才能访问
     */
    @GetMapping("/getRolesByName/{name}")
    public List<String> getRolesByName(@PathVariable String name) {
        return userService.getUserRoleInfo(name);
    }

    /**
     * 登录才能访问
     */
    @GetMapping("/getUserPermissions/{name}")
    public List<String> getUserPermissions(@PathVariable String name) {
        return userService.getUserPermissions(name);
    }

    /**
     * 登录且有admin角色才能访问
     */
    @RequiresRoles("admin")
    @GetMapping("/checkRoleByAdmin")
    public String checkRoleByAdmin() {
        Subject subject = SecurityUtils.getSubject();
        System.out.println("session user : " + subject.getSession().getAttribute("user"));
        return "验证角色( admin )成功";
    }

    /**
     * 登录且有manager角色才能访问
     */
    @RequiresRoles("manager")
    @GetMapping("/checkRoleByManager")
    public String checkRoleByManager() {
        return "验证角色( manager )成功";
    }


    @RequiresPermissions("user:add")
    @GetMapping("/checkHasAdd")
    public String checkHasAdd() {
        return "验证权限( user:add )成功";
    }

    @RequiresPermissions("user:update")
    @GetMapping("/checkHasUpdate")
    public String checkHasUpdate() {
        return "验证权限( user:update )成功";
    }

    @RequiresPermissions("user:delete")
    @GetMapping("/checkHasDelete")
    public String checkHasDelete() {
        return "验证权限( user:delete )成功";
    }
}

/**
 * @RequiresAuthentication 是否登录
 * @RequiresUser 是否记住用户
 * @RequiresRoles("admin") 是否是XX角色
 * @RequiresPermissions("account:add") 是否有XX权限
 */


/**
 * Subject subject =SecurityUtils.getSubject();
 * if(subject.hasRole("admin")){
 * //有权限
 * }else{
 * //无权限
 * }
 */
