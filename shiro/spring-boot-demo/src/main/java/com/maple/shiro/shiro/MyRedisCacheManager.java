package com.maple.shiro.shiro;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyRedisCacheManager implements CacheManager {

    @Autowired
    private MyRedisCache myRedisCache;


    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) throws CacheException {
        myRedisCache.setCacheName(cacheName);
        return myRedisCache;
    }
}
