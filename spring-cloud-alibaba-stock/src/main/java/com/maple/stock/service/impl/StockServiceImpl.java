package com.maple.stock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maple.stock.entity.Stock;
import com.maple.stock.mapper.StockMapper;
import com.maple.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 陈其丰
 */
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService{


    @Autowired
    private StockMapper stockMapper;

    @Override
    public void discount() throws Exception {
        Stock stock = stockMapper.selectById(1l);
        if (stock.getCount() > 0){
            stock.setCount(stock.getCount() - 1);
            stockMapper.updateById(stock);
        }else {
            throw new Exception("库存不足");
        }
    }
}
