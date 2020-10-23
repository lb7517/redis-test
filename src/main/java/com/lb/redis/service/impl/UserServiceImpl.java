package com.lb.redis.service.impl;
import com.lb.redis.component.cache.RedisCache;
import com.lb.redis.component.lock.RedisLock;
import com.lb.redis.constant.RedisKey;
import com.lb.redis.entity.User;
import com.lb.redis.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : lb
 * @date : 2020/9/11 16:17
 * @description :
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    RedisCache redisCache;

    @Autowired
    RedisLock redisLock;

    @Override
    public int insert(User user) {
        int id = user.getId();
        // key value形式保存
        redisCache.set(RedisKey.USER_INFO + id, user);
        User user1 = (User) redisCache.get(RedisKey.USER_INFO + id);
        log.info("user1: {}", user1);

        // 使用hash存储单条记录
        String key = String.valueOf(user.getId()+1);
        redisCache.putObject(RedisKey.USER_OBJ_INFO, key, user);
        User user2 = (User) redisCache.getObject(RedisKey.USER_OBJ_INFO, key);
        log.info("user2: {}", user2);

        // 使用hash存储多条记录
        Map<String, Object> map = new HashMap<>();
        map.put("111", user);
        map.put("222", user);
        redisCache.putObjects(RedisKey.USER_OBJ_INFO, map);
        List<User> users = (List<User>) redisCache.getObjects(RedisKey.USER_OBJ_INFO);
        log.info("users: {}", users);
        return 0;
    }

    @Override
    public List<User> query(User user) {
        List<User> users = (List<User>) redisCache.getObjects(RedisKey.USER_OBJ_INFO);
        log.info("users: {}", users);
        return users;
    }

    @Override
    public int update(User user) {
        String lockKey = "userLock";
        Long expireTime = 3000L;
        String value = String.valueOf(expireTime+System.currentTimeMillis());
        int id = user.getId();
        boolean lockFlag = redisLock.lock(lockKey, value);
        if(lockFlag){
            redisCache.set(RedisKey.USER_INFO + id, user);
            redisLock.unlock(lockKey, value);
        }
        return 0;
    }
}
