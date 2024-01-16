package com.maple.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.util.ByteSource;


public class MyRealm extends AuthenticatingRealm {
    /**
     * 自定义认证登录
     * 只是获取对比的信息
     * <p>
     * shiro的login方法底层会调用该方法
     * 需配置告诉框架自定义的Realm生效 - ini文件 / spring-boot
     * 该方法仅仅只是获取进行对比的信息, 认证逻辑还是按照shiro底层认证逻辑
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 1. 获取身份信息
        String principal = authenticationToken.getPrincipal().toString();
        // 2. 获取凭证信息
        String credentials = (String) authenticationToken.getCredentials();
        System.out.println("认证信息:" + principal + "------" + credentials);
        // 3. 访问数据库获取用户信息
        if (principal.equals("chenqifeng")) {
            // 3.1 从数据库中查询用户对应的密文密码
            String password = "180aca749787e2993b13e186d65a32f0"; // 加盐迭代3次后的密码
            // 4. 创建封装校验逻辑对象, 封装数据返回
            return new SimpleAuthenticationInfo(
                    authenticationToken.getPrincipal(),
                    password,
                    ByteSource.Util.bytes("demo"),
                    principal
            );
        }

        return null;
    }
}
