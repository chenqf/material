package com.maple.mp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maple.mp.entity.User;
import com.maple.mp.enums.SexEnum;
import com.maple.mp.mapper.UserMapper;
import com.maple.mp.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 陈其丰
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Integer insert() {
        User user = new User();
        user.setName("crh");
        user.setAge(33);
        user.setEmail("dddd@qq.com");
        user.setSex(SexEnum.FEMALE);
        return userMapper.insert(user);
    }

    @Override
    public Integer modify(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return userMapper.updateById(user);
    }

    @Override
    public List<User> queryByWrapper1() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("name","age") // 仅查询部分字段
                .like("name","a")
                .between("age",20,30)
                .isNotNull("email")
                .orderByDesc("age")
                .orderByAsc("id");
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public List<User> queryByWrapper2() {
        // 查询（年龄大于20并且用户名中包含有a）或邮箱为null的用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt("age",20)
                .like("name","a")
                .or()
                .isNull("email");
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public List<User> queryByWrapper3() {
        // 将用户名中包含有a并且（年龄大于20或邮箱为null）的用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name","a")
                .and(i->i.gt("age",20).or().isNull("email"));
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public List<User> queryByWrapper4() {
        // 实现子查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.inSql("id", "select id from t_user where id <= 3");
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public Integer updateByWrapper5() {
        // 基于条件更新
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("name","chenqf")
                .set("age",30)
                .eq("id",4);
        int result = this.getBaseMapper().update(null, updateWrapper);

        return result;
    }

    @Override
    public List<User> queryByWrapper6() {
        // 基于条件追加查询条件
        String username = null;
        Integer ageBegin = 10;
        Integer ageEnd = 24;
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .like(StringUtils.isNotBlank(username), "username", "a")
                .ge(ageBegin != null, "age", ageBegin)
                .le(ageEnd != null, "age", ageEnd);
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public List<User> queryByWrapper7() {
        // LambdaQueryWrapper
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(User::getAge,User::getName) // 仅查询部分字段
                .like(User::getName,"a")
                .between(User::getAge,20,30)
                .isNotNull(User::getEmail)
                .orderByDesc(User::getAge)
                .orderByAsc(User::getId);
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public Integer updateByWrapper8() {
        // LambdaUpdateWrapper
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getName,"chenqf")
                .set(User::getAge,30)
                .eq(User::getId,4);
        int result = this.getBaseMapper().update(null, updateWrapper);

        return result;
    }

    public void testVersion(){
//        User user = this.getBaseMapper().selectById(1L);
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getName,"chenqf1111")
                .set(User::getAge,30)
                .set(User::getEmail,"chenqf@qq2222.com")
                .eq(User::getId,4L);
        int result = this.getBaseMapper().update(null, updateWrapper);
    }

    @Override
    public void testPage() {
        Page<User> page = new Page<>(1, 2); // 第一页, 每页2条
        userMapper.selectPage(page, null);
        List<User> list = page.getRecords(); // 查询到的实体数据列表
        list.forEach(System.out::println);
        System.out.println("当前页："+page.getCurrent());
        System.out.println("每页显示的条数："+page.getSize());
        System.out.println("总记录数："+page.getTotal());
        System.out.println("总页数："+page.getPages());
        System.out.println("是否有上一页："+page.hasPrevious());
        System.out.println("是否有下一页："+page.hasNext());

    }

    @Override
    public void testPageForXml() {
        Page<User> page = new Page<>(1, 2); // 第一页, 每页2条
        userMapper.selectPageVo(page, 30);
        List<User> list = page.getRecords(); // 查询到的实体数据列表
        list.forEach(System.out::println);
        System.out.println("当前页："+page.getCurrent());
        System.out.println("每页显示的条数："+page.getSize());
        System.out.println("总记录数："+page.getTotal());
        System.out.println("总页数："+page.getPages());
        System.out.println("是否有上一页："+page.hasPrevious());
        System.out.println("是否有下一页："+page.hasNext());
    }
}
