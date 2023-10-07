package com.maple.seata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maple.seata.entity.Order;

/**
 * @author 陈其丰
 */
public interface OrderService extends IService<Order> {
    void createOrder();
}
