package com.maple.sharding.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 陈其丰
 */
@Data
@TableName("t_order")
public class Order {
    @TableId
    private Long id;
    private String name;
}
