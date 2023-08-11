package com.maple.sharding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.sharding.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author 陈其丰
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
