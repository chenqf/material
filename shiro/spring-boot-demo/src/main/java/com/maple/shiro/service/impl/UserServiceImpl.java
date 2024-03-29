package com.maple.shiro.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.maple.shiro.entity.User;
import com.maple.shiro.mapper.UserMapper;
import com.maple.shiro.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserByName(String name) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public List<String> getUserRoleInfo(String principal) {
        return userMapper.getUserRoleInfoMapper(principal);
    }

    @Override
    public List<String> getUserPermissions(String principal) {
        return userMapper.getUserPermissionsMapper(principal);
    }
}
