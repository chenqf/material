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
     * SELECT role.name
     * FROM user
     * JOIN role_user ON user.id = role_user.user_id
     * JOIN role ON role_user.role_id = role.id
     * WHERE user.name = 'cqf';
     */

    /**
     * SELECT distinct permissions.info
     * FROM user
     * JOIN role_user ON user.id = role_user.user_id
     * JOIN role ON role_user.role_id = role.id
     * JOIN role_permission ON role.id = role_permission.role_id
     * JOIN permissions ON role_permission.permission_id = permissions.id
     * WHERE user.name = 'cqf';
     */

    /**
     * 执行授权逻辑 - 判断 角色 和 权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("执行授权逻辑");
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
