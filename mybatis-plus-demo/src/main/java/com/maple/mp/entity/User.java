package com.maple.mp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.maple.mp.entity.base.BaseEntity;
import com.maple.mp.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author 陈其丰
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User extends BaseEntity {
    @TableId(value = "id")
    private Long id;
    private String name;
    private Integer age;
    @TableField("email")
    private String email;
    @TableLogic
    @TableField(exist = false) // 查询时不显示该字段
    private Integer isDelete;
    @Version
    private Integer version;
    private SexEnum sex;
}
