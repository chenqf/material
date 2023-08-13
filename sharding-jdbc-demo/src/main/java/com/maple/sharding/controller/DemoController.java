package com.maple.sharding.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maple.sharding.aop.ShardingMasterOnly;
import com.maple.sharding.entity.*;
import com.maple.sharding.mapper.*;
import lombok.Cleanup;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author 陈其丰
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseBookMapper courseBookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ValueSetItemMapper valueSetItemMapper;

    @Autowired
    private CourseDetailMapper courseDetailMapper;

    @GetMapping("/insertValueSetItem/{code}/{name}")
    public ValueSetItem insertValueSetItem(@PathVariable String code, @PathVariable String name){
        ValueSetItem item = new ValueSetItem();
        item.setCode(code);
        item.setName(name);
        item.setFkValueSetId(1L);
        this.valueSetItemMapper.insert(item);
        return item;
    }

    @GetMapping("/updateValueSetItem/{id}/{code}/{name}")
    public ValueSetItem updateValueSetItem(@PathVariable Long id, @PathVariable String code, @PathVariable String name){
        ValueSetItem item = new ValueSetItem();
        item.setCode(code);
        item.setName(name);
        item.setFkValueSetId(1L);
        LambdaUpdateWrapper<ValueSetItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ValueSetItem::getId,id);
        this.valueSetItemMapper.update(item,updateWrapper);
        return item;
    }

    @GetMapping("/insertUser")
    public User insertUser(){
        User user = new User();
        user.setName("chenqf");
        user.setPassword("123456");
        this.userMapper.insert(user);
        return user;
    }

    @GetMapping("/getUser/{name}/{password}")
    public List<User> getUser(@PathVariable String name, @PathVariable String password){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getName,name).eq(User::getPassword,password);
        return this.userMapper.selectList(queryWrapper);
    }

    @GetMapping("/getUser")
    @ShardingMasterOnly
    public List<User> getUser(){
        return this.userMapper.selectList(null);
    }

    @GetMapping("/getUser1")
    public List<User> getUser1(){
        List<User> users = this.userMapper.selectList(null);
        return users;
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

    @GetMapping("/insertDetail")
    public String insertDetail(){
        CourseDetail item = new CourseDetail();
        item.setDesc("dddd");
//        item.setFkCourseId(123123L);
        this.courseDetailMapper.insert(item);
        return "插入成功";
    }

    @GetMapping("/insertBook")
    public String insertBook(){
        CourseBook item = new CourseBook();
        item.setName("chenqf");
        item.setFkCourseId(123123L);
        this.courseBookMapper.insert(item);
        return "插入成功";
    }

    @GetMapping("/getItem")
    public List<Item> getItem(){
        return this.itemMapper.selectList(null);
    }

    @GetMapping("/insertCourseAndBook")
    public List insertCourse(){

        Course course = new Course();
        course.setName("语文");
        this.courseMapper.insert(course);

        CourseBook item = new CourseBook();
        item.setName("chenqf");
        item.setFkCourseId(course.getId());
        this.courseBookMapper.insert(item);

        return Arrays.asList(course,item);
    }

    @GetMapping("/getCourseAndBook/{courseId}")
    public String getCourseAndBook(@PathVariable Long courseId){
        this.courseMapper.selectCourseWithBooksById(courseId);
        return "查询成功";
    }



    @GetMapping("/delete")
    public String deleteCourse(){
        this.courseMapper.delete(null);
        this.courseBookMapper.delete(null);
//        this.courseDetailMapper.delete(null);
        return "删除成功";
    }

    @GetMapping("/getById/{id}")
    public Course getById(@PathVariable Long id){
        return this.courseMapper.selectById(id);
    }

    @GetMapping("/getByName/{name}")
    public Course getByName(@PathVariable String name){
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getName,name);
        return this.courseMapper.selectOne(queryWrapper);
    }

    @GetMapping("/getByIdAndUserId/{id}/{userId}")
    public Course getById(@PathVariable Long id,@PathVariable Long userId){
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getId,id).eq(Course::getUserId,userId);
        return this.courseMapper.selectOne(queryWrapper);
    }

    @GetMapping("/getByIdRangeUserId/{id}/{userId}")
    public Course getByIdRangeUserId(@PathVariable Long id,@PathVariable Long userId){
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getId,id).gt(Course::getUserId,userId);
        return this.courseMapper.selectOne(queryWrapper);
    }

    @GetMapping("/getIn/{ids}")
    public List<Course> getIn(@PathVariable String ids){
        String[] split = ids.split(",");
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        ArrayList<Long> longs = new ArrayList<>();
        for (String s : split) {
            longs.add(Long.parseLong(s));
        }
        queryWrapper.in("id", longs);
        List<Course> courses = this.courseMapper.selectList(queryWrapper);
        return courses;
    }

    @GetMapping("/getGt/{id}")
    public List<Course> getGt(@PathVariable Long id){
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt("id",id);
        List<Course> courses = this.courseMapper.selectList(queryWrapper);
        return courses;
    }

    @GetMapping("/getRange/{id1}/{id2}")
    public List<Course> getRange(@PathVariable Long id1,@PathVariable Long id2){
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("id",id1,id2);
        List<Course> courses = this.courseMapper.selectList(queryWrapper);
        return courses;
    }

    @GetMapping("/getAll")
    public List<Course> getAll(){
        List<Course> courses = this.courseMapper.selectList(null);
        return courses;
    }

    @GetMapping("/getAllByHint/{dbValue}/{tableValue}")
    public List<Course> getAllByHint(@PathVariable String dbValue, @PathVariable String tableValue){
        @Cleanup HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("course",dbValue); // m{dbValue}
        hintManager.addTableShardingValue("course",tableValue); // course_{tableValue}
        List<Course> courses = this.courseMapper.selectList(null);
        return courses;
    }

    @GetMapping("/getPage/{current}")
    public Page<Course> getPage(@PathVariable Long current){
        Page<Course> page = new Page(current,2);
        return this.courseMapper.selectPage(page,null);
    }

}
