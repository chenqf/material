package com.maple.sharding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈其丰
 */
@TableName
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueSetItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String parentCode;
    private Long fkValueSetId;
}
