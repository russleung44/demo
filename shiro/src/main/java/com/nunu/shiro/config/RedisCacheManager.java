package com.nunu.shiro.config;

import com.nunu.shiro.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class RedisCacheManager implements CacheManager {

    private static final String CACHE_PREFIX = "shiro:cache:";
    private final RedisUtil redisUtil;

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        return new RedisCache<>(CACHE_PREFIX, redisUtil);
    }

    @RequiredArgsConstructor
    public static class RedisCache<K, V> implements Cache<K, V> {

        private final String name;
        private final RedisUtil redisUtil;

        @Override
        public V get(K key) throws CacheException {
            return (V) redisUtil.get(name + key);
        }

        @Override
        public V put(K key, V value) throws CacheException {
            redisUtil.setEx(name + key, value, 1, TimeUnit.HOURS);
            return value;
        }

        @Override
        public V remove(K key) throws CacheException {
            redisUtil.delete(name + key);
            return null;
        }

        @Override
        public void clear() throws CacheException {
            redisUtil.delete(redisUtil.keys(name + "*"));
        }

        @Override
        public int size() {
            return redisUtil.keys(name + "*").size();
        }

        @Override
        public Set<K> keys() {
            return redisUtil.keys(name + "*").stream()
                    .map(key -> (K) key.replace(name, ""))
                    .collect(Collectors.toSet());
        }


        @Override
        public Collection<V> values() {
            return redisUtil.keys(name + "*").stream()
                    .map(redisUtil::get)
                    .map(value -> (V) value)
                    .collect(Collectors.toList());
        }
    }
}