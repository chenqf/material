package com.maple.shiro.shiro;

import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
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
     * 判断登录的cache-key
     */
    @Override
    protected Object getAuthenticationCacheKey(AuthenticationToken token) {
        return token.getPrincipal().toString() + "_authentication";
    }

    /**
     * 判断权限的cache-key
     */
    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        return principals.toString() + "_authorization";
    }


    /**
     * 执行授权逻辑 - 判断 角色 和 权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        // 1. 创建对象, 封装当前用户的角色/权限信息
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 2. 获取当前用户的身份信息
        String name = principalCollection.getPrimaryPrincipal().toString();
        // 3. 获取用户角色信息
        List<String> roles = userService.getUserRoleInfo(name);
        // 4. 存储角色
        info.addRoles(roles);
        // 5. 获取用户权限信息
        List<String> permissions = userService.getUserPermissions(name);
        // 6. 存储权限
        info.addStringPermissions(permissions);
        // 7. 返回信息
        return info;
    }


    /**
     * 执行认证逻辑 - 判断登录
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 1. 获取用户信息
        String name = authenticationToken.getPrincipal().toString();
        // 2. 调用业务层获取用户信息
        User user = userService.getUserByName(name);
        // 3. 非空判断, 将数据完成封装返回
        if (user != null) {
            return new SimpleAuthenticationInfo(
                    authenticationToken.getPrincipal(),
                    user.getPwd(),
//                    ByteSource.Util.bytes("salt-key"),
                    new MySimpleByteSource("salt-key"),
                    authenticationToken.getPrincipal().toString()
            );
        }
        return null;
    }
}
