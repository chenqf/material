package com.maple.sharding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maple.sharding.entity.Course;
import com.maple.sharding.entity.Item;
import com.maple.sharding.entity.Order;
import com.maple.sharding.entity.User;
import com.maple.sharding.mapper.CourseMapper;
import com.maple.sharding.mapper.ItemMapper;
import com.maple.sharding.mapper.OrderMapper;
import com.maple.sharding.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 陈其丰
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ItemMapper itemMapper;

    @GetMapping("/insertUser")
    public String insertUser(){
        User user = new User();
        user.setName("chenqf");
        this.userMapper.insert(user);
        return "插入成功";
    }



    @GetMapping("/getUser")
    public List<User> getUser(){
        return this.userMapper.selectList(null);
    }

    @GetMapping("/insertOrder")
    public String insertOrder(){
        Order order = new Order();
        order.setName("chenqf");
        this.orderMapper.insert(order);
        return "插入成功";
    }

    @GetMapping("/getOrder")
    public List<Order> getOrder(){
        return this.orderMapper.selectList(null);
    }


    @GetMapping("/insertItem")
    public String insertItem(){
        Item item = new Item();
        item.setName("chenqf");
        this.itemMapper.insert(item);
        return "插入成功";
    }

    @GetMapping("/getItem")
    public List<Item> getItem(){
        return this.itemMapper.selectList(null);
    }





    @GetMapping("/insert10")
    public String insert10(){
        for (int i = 0; i < 10; i++) {
            Course course = new Course();
            course.setName("course_" + i);
            this.courseMapper.insert(course);
        }
        return "插入成功";
    }

    @GetMapping("/getById")
    public Course getById(@PathVariable Long id){
        return this.courseMapper.selectById(id);
    }
}
