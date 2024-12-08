package com.nunu.shiro.config;

import com.nunu.shiro.RedisUtil;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisSessionDAO implements SessionDAO {

    private static final String SESSION_PREFIX = "shiro:session:";
    private final RedisUtil redisUtil;

    @Autowired
    public RedisSessionDAO(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public void update(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }
        redisUtil.setEx(SESSION_PREFIX + session.getId(), session, 1, TimeUnit.HOURS);
    }

    @Override
    public void delete(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }
        redisUtil.delete(SESSION_PREFIX + session.getId());
    }

    @Override
    public Session readSession(Serializable sessionId) {
        return (Session) redisUtil.get(SESSION_PREFIX + sessionId);
    }

    @Override
    public Serializable create(Session session) {
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        redisUtil.setEx(SESSION_PREFIX + sessionId, session, 1, TimeUnit.HOURS);
        return sessionId;
    }


    @Override
    public Collection<Session> getActiveSessions() {
        Set<String> keys = redisUtil.keys(SESSION_PREFIX + "*");
        List<Session> sessions = new ArrayList<>();
        for (String key : keys) {
            Session session = (Session) redisUtil.get(key);
            if (session != null) {
                sessions.add(session);
            }
        }
        return sessions;
    }


    protected Serializable generateSessionId(Session session) {
        return UUID.randomUUID().toString();
    }

    protected void assignSessionId(Session session, Serializable sessionId) {
        if (session instanceof SimpleSession) {
            ((SimpleSession) session).setId(sessionId);
        } else {
            session.setAttribute("id", sessionId);
        }
    }
}