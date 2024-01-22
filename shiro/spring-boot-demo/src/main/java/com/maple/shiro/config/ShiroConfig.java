package com.maple.shiro.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.maple.shiro.realm.MyRealm;

/**
 * @Description:shiro配置类
 */
@Configuration
public class ShiroConfig {
    /**
     * 创建ShiroFilterFactoryBean
     */
    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        //设置安全管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        //添加Shiro内置过滤器
        /**
         * Shiro内置过滤器，可以实现权限相关的拦截器
         *    常用的过滤器：
         *       anon: 无需认证（登录）可以访问
         *       authc: 必须认证才可以访问
         *       user: 如果使用rememberMe的功能可以直接访问
         *       perms： 该资源必须得到资源权限才可以访问
         *       role: 该资源必须得到角色权限才可以访问
         */
        Map<String, String> filterMap = new LinkedHashMap<String, String>();

        filterMap.put("/auth/notLogin", "anon");
        filterMap.put("/auth/noRoleOrNoPermission", "anon");
        filterMap.put("/auth/login", "anon");
        filterMap.put("/auth/logout", "logout");

        filterMap.put("/**", "authc");
        filterMap.put("/**", "user");

        // 要求登陆时的链接，非必须。
        shiroFilterFactoryBean.setLoginUrl("/auth/notLogin");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 创建DefaultWebSecurityManager
     */
    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager(MyRealm myRealm) {
        // 1. 创建 DefaultWebSecurityManager
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 2. 创建加密对象, 设置相关属性
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
        matcher.setHashAlgorithmName("md5");
        matcher.setHashIterations(3);
        // 3. 将加密对象存储到MyRealm中
        myRealm.setCredentialsMatcher(matcher);
        // 4. 将MyRealm存入DefaultWebSecurityManager
        securityManager.setRealm(myRealm);
        // 5. 设置 remember me
        securityManager.setRememberMeManager(rememberMeManager());
        return securityManager;
    }

    public SimpleCookie rememberMeCookie() {
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(30 * 24 * 60 * 60);
        return cookie;
    }

    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        cookieRememberMeManager.setCipherKey("1234567890987654".getBytes());
        return cookieRememberMeManager;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(MyRealm myRealm) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(getDefaultWebSecurityManager(myRealm));
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
        Properties properties = new Properties();
        //未授权的网页跳转至error.html
        properties.setProperty("org.apache.shiro.authz.UnauthorizedException", "/auth/noRoleOrNoPermission");
        resolver.setExceptionMappings(properties);
        return resolver;
    }


//    @Bean
//    public DefaultSecurityManager securityManager(MyRealm myRealm, RedisCacheManager redisCacheManager) {
//        // 1. 创建 DefaultWebSecurityManager
//        DefaultSecurityManager securityManager = new DefaultWebSecurityManager();
//        // 2. 创建加密对象, 设置相关属性
//        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
//        matcher.setHashAlgorithmName("md5");
//        matcher.setHashIterations(3);
//        // 3. 将加密对象存储到MyRealm中
//        myRealm.setCredentialsMatcher(matcher);
//        // 4. 将MyRealm存入DefaultWebSecurityManager
//        securityManager.setRealm(myRealm);
//        // 5. 设置 remember me
//        securityManager.setRememberMeManager(rememberMeManager());
//        // 6. 添加缓存管理
//        securityManager.setCacheManager((CacheManager) redisCacheManager);
//        return securityManager;
//    }
}


