package com.maple.sharding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 陈其丰
 */
@Data
@TableName
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String password;
}
