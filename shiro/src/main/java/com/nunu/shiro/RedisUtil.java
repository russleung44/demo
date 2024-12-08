package com.nunu.shiro;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // 设置键值对
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 设置键值对并设置过期时间
    public void setEx(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 获取键值对
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 删除键
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 删除多个键
    public void delete(Set<String> keys) {
        redisTemplate.delete(keys);
    }

    // 获取所有匹配的键
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    // 检查键是否存在
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // 设置键的过期时间
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    // 获取键的剩余过期时间
    public long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }
}