package com.maple.task.filter;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * @author 陈其丰
 */
public class TraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String traceId = httpRequest.getHeader("Trace-Id");
        if("".equals(traceId) || null == traceId){
            traceId = generateTraceId();
        }
        MDC.put("traceId", traceId);
        chain.doFilter(request, response);
        MDC.remove("traceId");
    }
    // 生成Trace ID的方法
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
