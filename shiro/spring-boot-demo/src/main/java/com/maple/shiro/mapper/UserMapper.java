package com.maple.shiro.mapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.shiro.entity.User;

@Repository
public interface UserMapper extends BaseMapper<User> {
}
