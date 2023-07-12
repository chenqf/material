package com.maple.lock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.lock.pojo.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author 陈其丰
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {
    @Update("update stock set count = count - #{count} where id = #{id} and count >= #{count}")
    int pessimisticSqlDeduct(@Param("id") Integer id, @Param("count") int count);

    @Select("select * from stock where id=#{id} for update")
    Stock pessimisticSelectForUpdateDeduct(Integer id);
}
