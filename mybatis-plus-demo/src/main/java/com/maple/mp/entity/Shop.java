package com.maple.mp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.maple.mp.entity.base.BaseEntity;
import com.maple.mp.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈其丰
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("shop")
public class Shop{
    @TableId(value = "id")
    private Long id;
    private String name;
    private Long tenantId;
}
