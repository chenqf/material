package com.maple.sharding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.sharding.dto.CourseWithBooks;
import com.maple.sharding.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author 陈其丰
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    @Select("SELECT c.*, cb.* " +
            "FROM course c " +
            "LEFT JOIN book cb ON c.id = cb.fk_course_id " +
            "WHERE c.id = #{courseId}")
    void selectCourseWithBooksById(@Param("courseId") Long courseId);

}
