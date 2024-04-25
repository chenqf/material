package com.maple.shiro.shiro;

import java.io.Serializable;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisSessionDAO extends CachingSessionDAO implements SessionDAO {

    @Autowired
    @Qualifier("shiroRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doUpdate(Session session) {
        if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
            return;
        }
        saveSession(session);
    }

    @Override
    protected void doDelete(Session session) {
        redisTemplate.delete(getRedisKey(session.getId()));
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        saveSession(session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        return (Session) redisTemplate.opsForValue().get(getRedisKey(sessionId));
    }

    private void saveSession(Session session) {
        if (session != null && session.getId() != null) {
            String key = getRedisKey(session.getId());
            redisTemplate.opsForValue().set(key, session);
        }
    }

    private String getRedisKey(Serializable sessionId) {
        return "shiro:session:" + sessionId;
    }
}
