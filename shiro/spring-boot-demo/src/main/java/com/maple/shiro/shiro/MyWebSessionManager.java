package com.maple.shiro.shiro;

import java.io.Serializable;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.util.StringUtils;

public class MyWebSessionManager extends DefaultWebSessionManager {

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        Serializable sessionId = req.getHeader("token");
        System.out.println("sessionId : " + sessionId);
        System.out.println("parent sessionId : " + super.getSessionId(request, response));
        if (!StringUtils.isEmpty(sessionId)) {
            System.out.println("use my session manager sessionId !!!");
            return sessionId;
        }
        return super.getSessionId(request, response);
    }

    public void closeCookie() {
        super.setSessionIdCookieEnabled(false);
    }
}
