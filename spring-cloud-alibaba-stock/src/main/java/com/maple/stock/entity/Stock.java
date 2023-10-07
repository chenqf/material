package com.maple.stock.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈其丰
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("stock")
public class Stock {
    @TableId(value = "id")
    private Long id;
    private String name;
    private Long count;
}
