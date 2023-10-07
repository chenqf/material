package com.maple.seata.mapper;

import com.maple.seata.entity.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 陈其丰
 */
@Mapper
public interface AccountMapper{
    @Insert("insert into account  values(#{id},#{name},#{money}))")
    void insertAccount(Account account);
}
