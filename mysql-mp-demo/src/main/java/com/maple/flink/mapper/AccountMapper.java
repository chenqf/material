package com.maple.flink.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.flink.entity.Account;

/**
 * @author 陈其丰
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

}
