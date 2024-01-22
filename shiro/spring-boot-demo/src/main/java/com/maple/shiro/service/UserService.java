package com.maple.shiro.service;

import java.util.List;

import com.maple.shiro.entity.User;


public interface UserService {

    User getUserByName(String name);

    List<String> getUserRoleInfo(String principal);

    List<String> getUserPermissions(String principal);
}
