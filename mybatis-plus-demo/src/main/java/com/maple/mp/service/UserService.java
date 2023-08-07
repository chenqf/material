package com.maple.mp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maple.mp.entity.User;

import java.util.List;

/**
 * @author 陈其丰
 */
public interface UserService extends IService<User> {

    Integer insert();
    Integer modify(Long id, String name);
    List<User> queryByWrapper1();

    List<User> queryByWrapper2();
    List<User> queryByWrapper3();
    List<User> queryByWrapper4();
    Integer updateByWrapper5();
    List<User> queryByWrapper6();
    List<User> queryByWrapper7();
    Integer updateByWrapper8();

    void testVersion();

    void testPage();

    void testPageForXml();

    User findUserById(Long id);
}
