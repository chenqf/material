package com.maple.flink.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.maple.flink.entity.Account;
import com.maple.flink.mapper.AccountMapper;

/**
 * @author 陈其丰
 */
@Service
public class DemoService {

    @Autowired
    private AccountMapper accountMapper;

    private final TransactionTemplate transactionTemplate;

    public DemoService(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void manualTransaction(){
        transactionTemplate.execute(status -> {
            try{
                this.dbOperation();
            }catch (Exception e){
                status.setRollbackOnly();
                throw e;
            }
            return null;
        });

    }

    @Transactional
    public void autoTransaction(){
        this.dbOperation();
    }

    public void dbOperation(){
        Account account1 = accountMapper.selectById(1L);
        Account account2 = accountMapper.selectById(2L);

        account1.setMoney(account1.getMoney() - 100);
        account2.setMoney(account2.getMoney() + 100);

        accountMapper.updateById(account1);
        int i = 10/0;
        accountMapper.updateById(account2);
    }
}
