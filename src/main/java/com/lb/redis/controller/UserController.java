package com.lb.redis.controller;

import com.lb.redis.entity.User;
import com.lb.redis.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : lb
 * @date : 2020/9/11 16:18
 * @description :
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    @PostMapping("/add")
    public Map<String, Object> insert(@RequestBody User user){
        int result = userService.insert(user);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 1000);
        map.put("data", result);
        return map;
    }

    @PostMapping("/query")
    public Map<String, Object> query(@RequestBody User user){
        List<User> result = userService.query(user);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 1000);
        map.put("data", result);
        return map;
    }
}
