package com.maple.mp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.maple.mp.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer isDelete;
    @Version
    private Integer version;
}
