package com.maple.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maple.stock.entity.Stock;

import java.util.List;

/**
 * @author 陈其丰
 */
public interface StockService extends IService<Stock> {
    void discount() throws Exception;
}
