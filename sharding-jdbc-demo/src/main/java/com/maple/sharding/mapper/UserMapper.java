package com.maple.sharding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.sharding.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 陈其丰
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
