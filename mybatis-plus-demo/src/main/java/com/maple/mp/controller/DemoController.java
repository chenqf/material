package com.maple.mp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maple.mp.entity.Shop;
import com.maple.mp.entity.User;
import com.maple.mp.mapper.ShopMapper;
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
    @Autowired
    private ShopMapper shopMapper;

    @GetMapping("/insert")
    public Integer insert(){
        return this.userService.insert();
    }

    @GetMapping("/modify")
    public Integer modify(@RequestParam("id") Long id ,@RequestParam("name") String name){
        return this.userService.modify(id,name);
    }


    @GetMapping("/users")
    public List<User> users(){
        List<User> users = userService.queryByWrapper3();
        users.forEach(System.out::println);
        return users;
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

    @GetMapping("/test10")
    public String test10(){
        User user = new User();
        user.setId(999L);
        user.setName("custom_name");
        user.setEmail("xxx@mail.com");
        // com.baomidou.mybatisplus.core.exceptions.MybatisPlusException: Prohibition of table update operation
        userService.saveOrUpdate(user, null);
        return "111111";
    }

    @GetMapping("/test11")
    public User test11(){
        return userService.findUserById(3L);
    }

    @GetMapping("/shop1")
    public List<Shop> shop1(){
        return shopMapper.selectList(null);
    }

    @GetMapping("/shop2")
    public Shop shop2(){
        return shopMapper.findShopById(1L);
    }

    @GetMapping("/shop3")
    public Page<Shop> shop3(){
        Page<Shop> page = new Page<>(1, 2); // 第一页, 每页2条
        shopMapper.selectPage(page,null);
        List<Shop> list = page.getRecords(); // 查询到的实体数据列表
        list.forEach(System.out::println);
        System.out.println("当前页："+page.getCurrent());
        System.out.println("每页显示的条数："+page.getSize());
        System.out.println("总记录数："+page.getTotal());
        System.out.println("总页数："+page.getPages());
        System.out.println("是否有上一页："+page.hasPrevious());
        System.out.println("是否有下一页："+page.hasNext());
        return page;
    }
}
