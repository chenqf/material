package com.maple.sharding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.maple.sharding.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈其丰
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName
public class Course extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long userId;
}
