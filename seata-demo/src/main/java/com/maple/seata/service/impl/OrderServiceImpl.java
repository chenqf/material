package com.maple.seata.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maple.seata.entity.Order;
import com.maple.seata.feign.StockFeignService;
import com.maple.seata.mapper.OrderMapper;
import com.maple.seata.service.OrderService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * @author 陈其丰
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper,Order> implements OrderService{

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StockFeignService stockFeignService;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void createOrder() {

        String xid = RootContext.getXID();

        // 创建订单
        Order order = new Order();
        order.setOrderName(UUID.randomUUID().toString());
        orderMapper.insert(order);
        // 调用其他服务, 扣减库存
        stockFeignService.discount();
    }
}
