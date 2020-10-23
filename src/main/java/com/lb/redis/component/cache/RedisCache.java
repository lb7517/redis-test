package com.lb.redis.component.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : lb
 * @date : 2020/9/11 15:24
 * @description : redis工具类
 */
@Component
public class RedisCache {

    /**
     * 注意注入RedisTemplate时名称要与@Bean中的方法名一致或者使用@Qualifier注解指明@Bean的名称，否者默认使用springboot容器中自带的bean,
     * 自带的是使用Jdk序列化的，这种序列化要求value需要实现序列化； 此处使用的是自定义序列化
     * */
    @Autowired
    @Qualifier("setRedisTemplate")
    private RedisTemplate redisTemplate;

    /**
     * 设置缓存, 不设置过期时间
     * */
    public void set(String key, Object value){
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存， 设置过期时间
     * */
    public void set(String key, Object value, Long timeout){
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存， 设置过期时间, 如果建不存在则新增，存在则不改变已有的值
     * 相当于 redis 中的 setnx 的方法
     * */
    public boolean setIfAbsent(String key, Object value, Long timeout){
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存， 设置过期时间, 如果键不存在则新增，存在则不改变已有的值
     * 相当于 redis 中的 setnx 的方法
     * */
    public boolean setIfAbsent(String key, String value){
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 获取指定key的值
     * */
    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置新值返回旧值
     * */
    public Object getAndSet(String key, String value){
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    /**
     * 设置某个key的过期时间
     * */
    public boolean expire(String key, Long ttl) {
        return redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

    /**
     * 判断某个key是否存在
     * */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获取对象
     * */
    public Object getObject(String key, String smallKey){
        //大key， 小key, 值
        return redisTemplate.opsForHash().get(key, smallKey);
    }

    /**
     * 添加单条对象,不设置过期时间
     * */
    public void putObject(String key, String smallKey, Object obj){
        //大key， 小key, 值
        redisTemplate.opsForHash().put(key, smallKey, obj);
    }

    /**
     * 添加单条对象,置过期时间
     * */
    public void putObject(String key, String smallKey, Object obj, Long ttl){
        //大key， 小key, 值
        redisTemplate.opsForHash().put(key, smallKey, obj);
        expire(key, ttl); //第三个参数是单位
    }

    /**
     * 获取对象列表，通过大key
     * */
    public Object getObjects(String key){
        //大key， 小key, 值
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 添加多条对象，事务添加(redis-cluster 多节点集群不支持)
     * */
    public void putObjects(String key, Map obj){
        //大key， 小key, 值
        /*SessionCallback sessionCallback = new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                redisTemplate.opsForHash().putAll(key, obj);
                Object result = operations.exec();
                return result;
            }
        };
        redisTemplate.execute(sessionCallback);*/
        redisTemplate.opsForHash().putAll(key, obj);
    }

    /**
     * 通过key删除缓存
     * */
    public boolean del(String key) {
        return redisTemplate.opsForValue().getOperations().delete(key);
    }

}
