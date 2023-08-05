package com.maple.mp.entity.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author 陈其丰
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {
    @TableField(fill = FieldFill.INSERT) // 插入时处理
    private String createBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时处理
    private String updateBy;
    @TableField(fill = FieldFill.INSERT) // 插入时处理
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时处理
    private Date updateTime;
}
