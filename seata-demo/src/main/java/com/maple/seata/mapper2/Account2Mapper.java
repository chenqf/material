package com.maple.seata.mapper2;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.seata.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author 陈其丰
 */
//@Mapper
public interface Account2Mapper extends BaseMapper<Account> {

    @Update("update account set money=money+#{money} where id = 2")
    void addMoney(double money);
}
