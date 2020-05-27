package org.webworkbench.cache.pool;

import org.webworkbench.cache.BaerCache;

import java.util.Map;

/**
 * 缓存池
 */
public class CachePool {

    /**
     * 唯一缓存池实例
     */
    private static CachePool instance;

    private static Map<String, BaerCache> baerCacheMap;

    /**
     * 静态构造方法，避免手动创建缓存池
     */
    private CachePool(){}

    /**
     * 获取缓存池实例，如果线缓存不存在则创建一个缓存池。
     * @return 线程池实例
     */
    public synchronized static CachePool getInstance(){
        if(instance == null){
            instance = new CachePool();
        }
        return instance;
    }

    public static BaerCache getBaerCache(String name){
        return baerCacheMap.get(name);
    }
}
