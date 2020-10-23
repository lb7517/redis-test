package com.lb.redis.component.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author : lb
 * @date : 2020/10/23 16:49
 * @description : 通过Redission调用红锁
 */
@Component
@Slf4j
public class RedLock {

    /**
     * RedissonClient已经由配置类生成，这里自动装配即可
     * */
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 加锁
     * */
    public Boolean lock(String lockName) {
        try {
            if (redissonClient == null) {
                log.info("DistributedRedisLock redissonClient is null");
                return false;
            }
            RLock lock = redissonClient.getLock(lockName);
            // 锁10秒后自动释放，防止死锁
            lock.lock(10, TimeUnit.SECONDS);
           /* log.info("Thread [{}] DistributedRedisLock lock [{}] success",
                    Thread.currentThread().getName(), lockName);*/
            // 加锁成功
            return true;
        } catch (Exception e) {
            log.error("DistributedRedisLock lock [{}] Exception:", lockName, e);
            return false;
        }
    }

    /**
     * 释放锁
     * */
    public Boolean unlock(String lockName) {
        try {
            if (redissonClient == null) {
                log.info("DistributedRedisLock redissonClient is null");
                return false;
            }
            RLock lock = redissonClient.getLock(lockName);
            lock.unlock();
            /*log.info("Thread [{}] DistributedRedisLock unlock [{}] success",
                    Thread.currentThread().getName(), lockName);*/
            // 释放锁成功
            return true;
        } catch (Exception e) {
            log.error("DistributedRedisLock unlock [{}] Exception:", lockName, e);
            return false;
        }
    }

}
