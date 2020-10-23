package com.lb.redis.component.lock;

import com.lb.redis.component.cache.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author : lb
 * @date : 2020/9/22 11:16
 * @description : redis分布式锁
 */
@Component
@Slf4j
public class RedisLock {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    @Qualifier("setRedisTemplate")
    private RedisTemplate redisTemplate;

    private static final Long SUCCESS = 1L;

    //定义获取锁的lua脚本， KEYS[1]表示key, ARGV[1]表示content, ARGV[2]过期时间
    private final static DefaultRedisScript<Long> LOCK_LUA_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then return redis.call('pexpire', KEYS[1], " +
                    "ARGV[2]) else return 0 end"
            , Long.class
    );

    //定义释放锁的lua脚本
    private final static DefaultRedisScript<Long> UNLOCK_LUA_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) " +
                    "else return -1 end"
            , Long.class
    );

    /**
     * 方式一:(redis服务有宕机或有问题)
     * 加锁(这里对setnx即setIfAbsent时候的线程中断或者其他异常导致的死锁进行了相应的处理)
     * @param key 锁唯一标志
     * @param value 当前时间+超时时间
     * @return
     * */
    public boolean lock(String key, String value) {
        if(redisCache.setIfAbsent(key, value)){
            return true;
        }
        // 判断锁超时，防止死锁
        String currentValue = (String) redisCache.get(key);
        // 如果锁过期
        if(!StringUtils.isEmpty(currentValue) && Long.valueOf(currentValue) < System.currentTimeMillis()){
            // 设置当前value，并获取上一个锁的时间value
            String oldValue = (String) redisCache.getAndSet(key, value);
            // 是否被别人抢占了
            if(!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)){
                return true;
            }
        }
        return false;
    }

    /**
     * 解锁
     * */
    public void unlock(String key, String value) {
        String currentValue = (String) redisCache.get(key);
        if(!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
            boolean result = redisCache.del(key);
            if(result){
                log.info("解锁成功 value: " + value);
            }
        }
    }

    /**
     * 方式二:
     * 获取分布式锁，原子操作
     * @param lockKey
     * @param expire
     * @param timeUnit
     * @return
     */
    public boolean tryLock(String lockKey, String value, long expire, TimeUnit timeUnit) {
        try{
            //组装lua脚本参数
            List<String> keys = Arrays.asList(lockKey);
            Object result = redisTemplate.execute(LOCK_LUA_SCRIPT, keys, value, expire);
            if(SUCCESS.equals(result)){
                return true;
            }
        }catch(Exception e){
            log.error("异常: {}", e);
        }
        return false;
    }

    /**
     * 释放锁
     * @param lockKey
     * @param value
     * @return
     */
    public boolean releaseLock(String lockKey, String value) {
        //组装lua脚本参数
        List<String> keys = Arrays.asList(lockKey);
        Object result = redisTemplate.execute(UNLOCK_LUA_SCRIPT, keys, value);
        if(SUCCESS.equals(result)) {
            log.info("解锁成功 value: " + value);
            return true;
        }
        return false;
    }


}
