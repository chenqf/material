package com.maple.seata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.seata.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 陈其丰
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
