package com.maple.seata.service;

import com.maple.seata.entity.Account;
import com.maple.seata.mapper1.Account1Mapper;
import com.maple.seata.mapper2.Account2Mapper;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 陈其丰
 */
//@Service
public class XAMultService {

    @Autowired
    private Account1Mapper account1Mapper;

    @Autowired
    private Account2Mapper account2Mapper;

    @GlobalTransactional(rollbackFor = Exception.class)
    public void operationMoney1(){
        this.account1Mapper.reduceMoney(200);
        int i = 10/0;
        this.account2Mapper.addMoney(200);
    }

    public void operationMoney2(){
        Account account1 = this.account1Mapper.selectById(1);
        account1.setMoney(account1.getMoney() - 200);
        Account account2 = this.account1Mapper.selectById(2);
        account1.setMoney(account1.getMoney() + 200);
        this.account1Mapper.updateById(account1);
        this.account2Mapper.updateById(account2);
    }
}
