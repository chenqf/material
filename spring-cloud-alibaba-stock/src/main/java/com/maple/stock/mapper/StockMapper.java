package com.maple.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.stock.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 陈其丰
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}
