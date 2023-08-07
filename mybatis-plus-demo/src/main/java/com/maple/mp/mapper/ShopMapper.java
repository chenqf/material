package com.maple.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maple.mp.entity.Shop;
import com.maple.mp.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author 陈其丰
 */
@Mapper
public interface ShopMapper extends BaseMapper<Shop> {
    Shop findShopById(@Param("id") Long id);
}