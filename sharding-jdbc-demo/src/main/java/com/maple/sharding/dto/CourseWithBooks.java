package com.maple.sharding.dto;

import com.maple.sharding.entity.CourseBook;
import com.maple.sharding.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈其丰
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseWithBooks {
    private Course course;
    private CourseBook courseBook;
}
