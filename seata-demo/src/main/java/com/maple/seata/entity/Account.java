package com.maple.seata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈其丰
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("account")
public class Account {
    @TableId(value = "id")
    private Long id;
    private String name;
    private Double money;
}
