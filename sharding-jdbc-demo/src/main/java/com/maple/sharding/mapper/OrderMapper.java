package com.maple.sharding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.sharding.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 陈其丰
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
