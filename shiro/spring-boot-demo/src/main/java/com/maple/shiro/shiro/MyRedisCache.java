package com.maple.shiro.shiro;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MyRedisCache<K, V> implements Cache<K, V> {

    @Autowired
    @Qualifier("shiroRedisTemplate")
    private RedisTemplate redisTemplate;

    private String cacheName;

    public MyRedisCache(String cacheName) {
        this.cacheName = cacheName;
    }

    public MyRedisCache() {
    }

    public void setCacheName(String name) {
        this.cacheName = name;
    }

    //    @Override
//    public V get(K k) throws CacheException {
//        return (V) redisTemplate.opsForHash().get(this.cacheName, k.toString());
//    }
    @Override
    public V get(K k) throws CacheException {
        return (V) redisTemplate.opsForValue().get(this.cacheName + "-" + k.toString());
    }

//    @Override
//    public V put(K k, V v) throws CacheException {
//        redisTemplate.opsForHash().put(this.cacheName, k.toString(), v);
//        return null;
//    }

    @Override
    public V put(K k, V v) throws CacheException {
        redisTemplate.opsForValue().set(this.cacheName + "-" + k.toString(), v, 8, TimeUnit.HOURS);
//        redisTemplate.opsForValue().set(this.cacheName + "-" + k.toString(), v);
        return null;
    }

//    @Override
//    public V remove(K k) throws CacheException {
//
//        return (V) redisTemplate.opsForHash().delete(this.cacheName, k.toString());
//    }

    @Override
    public V remove(K k) throws CacheException {
        redisTemplate.opsForValue().getAndDelete(this.cacheName + "-" + k.toString());
//        redisTemplate.opsForValue().set(this.cacheName + "-" + k.toString(), null, -1);
        return null;
    }

    @Override
    public void clear() throws CacheException {
//        redisTemplate.opsForHash().delete(this.cacheName);
    }

    @Override
    public int size() {
//        return redisTemplate.opsForHash().size(this.cacheName).intValue();
        return 0;
    }

    @Override
    public Set<K> keys() {
//        return redisTemplate.opsForHash().keys(this.cacheName);
        return null;
    }

    @Override
    public Collection<V> values() {
//        return redisTemplate.opsForHash().values(this.cacheName);
        return null;
    }
}
