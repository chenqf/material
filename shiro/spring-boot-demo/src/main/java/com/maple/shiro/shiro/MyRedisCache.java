package com.maple.shiro.shiro;

import java.util.Collection;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MyRedisCache<K, V> implements Cache<K, V> {

    @Autowired
    private RedisTemplate redisTemplate;
    private String cacheName;

    public MyRedisCache setKey(String cacheName) {
        this.cacheName = cacheName;
        return this;
    }


    @Override
    public V get(K k) throws CacheException {
        return (V) redisTemplate.opsForHash().get(this.cacheName, k.toString());

    }

    @Override
    public V put(K k, V v) throws CacheException {

        redisTemplate.opsForHash().put(this.cacheName, k.toString(), v);
        return null;
    }

    @Override
    public V remove(K k) throws CacheException {

        return (V) redisTemplate.opsForHash().delete(this.cacheName, k.toString());
    }

    @Override
    public void clear() throws CacheException {
        redisTemplate.opsForHash().delete(this.cacheName);
    }

    @Override
    public int size() {
        return redisTemplate.opsForHash().size(this.cacheName).intValue();
    }

    @Override
    public Set<K> keys() {
        return redisTemplate.opsForHash().keys(this.cacheName);
    }

    @Override
    public Collection<V> values() {
        return redisTemplate.opsForHash().values(this.cacheName);
    }
}
