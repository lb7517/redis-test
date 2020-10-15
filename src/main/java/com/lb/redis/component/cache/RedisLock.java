package com.lb.redis.component.cache;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    // todo 使用redisson不是这样做的，要通过组件实例化bean
//    @Autowired
//    private RedissonClient redissonClient;

    /**
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
                log.info("解锁成功");
            }
        }
    }

}
