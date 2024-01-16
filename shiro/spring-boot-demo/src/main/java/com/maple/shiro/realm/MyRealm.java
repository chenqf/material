package com.maple.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.maple.shiro.entity.User;
import com.maple.shiro.service.UserService;

/**
 * @Description:自定义Realm 处理登录 权限
 */
@Component
public class MyRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;


    /**
     * 执行授权逻辑
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("执行授权逻辑");
        return null;
    }

    /**
     * 执行认证逻辑
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("start 执行认证逻辑");
        // 1. 获取用户信息
        String name = authenticationToken.getPrincipal().toString();
        // 2. 调用业务层获取用户信息
        User user = userService.getUserByName(name);
        // 3. 非空判断, 将数据完成封装返回
        if (user != null) {
            return new SimpleAuthenticationInfo(
                    authenticationToken.getPrincipal(),
                    user.getPwd(),
                    ByteSource.Util.bytes("salt-key"),
                    authenticationToken.getPrincipal().toString()
            );
        }
        return null;
    }
}
