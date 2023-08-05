package com.maple.mp.controller;

import com.maple.mp.entity.User;
import com.maple.mp.mapper.UserMapper;
import com.maple.mp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 陈其丰
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private UserService userService;

    @GetMapping("/insert")
    public Integer insert(){
        return this.userService.insert();
    }

    @GetMapping("/modify")
    public Integer modify(@RequestParam("id") Long id ,@RequestParam("name") String name){
        return this.userService.modify(id,name);
    }


    @GetMapping("/test1")
    public String test1(){
        List<User> users = userService.queryByWrapper1();
        users.forEach(System.out::println);
        return "111111";
    }

    @GetMapping("/test4")
    public String test4(){
        List<User> users = userService.queryByWrapper4();
        users.forEach(System.out::println);
        return "111111";
    }

    @GetMapping("/test5")
    public String test5(){
        userService.updateByWrapper5();
        return "111111";
    }

    @GetMapping("/test6")
    public String test6(){
        userService.updateByWrapper8();
        return "111111";
    }

    @GetMapping("/test7")
    public String test7(){
        userService.testVersion();
        return "111111";
    }

    @GetMapping("/test8")
    public String test8(){
        userService.testPage();
        return "111111";
    }

    @GetMapping("/test9")
    public String test9(){
        userService.testPageForXml();
        return "111111";
    }
}
