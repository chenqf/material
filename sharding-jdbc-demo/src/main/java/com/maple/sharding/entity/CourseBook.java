package com.maple.sharding.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 陈其丰
 */
@Data
@TableName
public class CourseBook {
    @TableId
    private Long id;
    private String name;
    private Long fkCourseId;
}
