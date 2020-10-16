package com.lb.redis.service;

import com.lb.redis.component.cache.RedisCache;
import com.lb.redis.component.cache.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : lb
 * @date : 2020/10/16 11:18
 * @description :
 */
@Service
public class RedisServiceTest {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisLock redisLock;

    public void set (String key, int value) {
        redisCache.set(key, value);
    }

    public int get (String key){
        return (int) redisCache.get(key);
    }

    /**
     * 通过使用redis分布式锁(1. 此处使用redis setnx指令实现；
     * 2. 也可以使用Redission实现，这种方式可以规避服务宕机使用setnx指令加锁异常)，买票
     * return 0/1 成功失败
     * */
    public int buyTicket(){
        // 加锁
        String key = "buyTicket";
        Long expireTime = 3000L;
        String value = String.valueOf(expireTime+System.currentTimeMillis());
        if(redisLock.lock(key, value)){
            int count = (int) redisCache.get("count");
            count--;
            redisCache.set("count", count);
            System.out.println("当前线程: "+ Thread.currentThread().getName()
                    + ", 当前票数: " + count + " , value: "+ value);
            redisLock.unlock(key, value);
            return 1;
        }
        return 0;
    }

    /**
     * 通过使用java 锁，买票(适合单实例)
     * return 0/1 成功失败
     * */
    public synchronized void buyTicket2(){
        int count = (int) redisCache.get("count");
        count--;
        redisCache.set("count", count);
        System.out.println("当前线程: "+ Thread.currentThread().getName()
                + ", 当前票数: " + count);
    }
}
