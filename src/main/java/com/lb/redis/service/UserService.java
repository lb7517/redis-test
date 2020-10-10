package com.lb.redis.service;

import com.lb.redis.entity.User;
import java.util.List;

/**
 * @author : lb
 * @date : 2020/9/11 16:16
 * @description :
 */
public interface UserService {
    int insert(User user);
    List<User> query(User user);
    int update(User user);
}
