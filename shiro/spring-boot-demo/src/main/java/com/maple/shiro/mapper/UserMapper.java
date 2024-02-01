package com.maple.shiro.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maple.shiro.entity.User;

@Repository
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT role.name\n" +
            "FROM user\n" +
            "JOIN role_user ON user.id = role_user.user_id\n" +
            "JOIN role ON role_user.role_id = role.id\n" +
            "WHERE user.name = #{principal}")
    List<String> getUserRoleInfoMapper(@Param("principal") String principal);

    @Select("SELECT distinct permission.info\n" +
            "FROM user\n" +
            "JOIN role_user ON user.id = role_user.user_id\n" +
            "JOIN role ON role_user.role_id = role.id\n" +
            "JOIN role_permission ON role.id = role_permission.role_id\n" +
            "JOIN permission ON role_permission.permission_id = permission.id\n" +
            "WHERE user.name = #{principal}")
    List<String> getUserPermissionsMapper(@Param("principal") String principal);
}
