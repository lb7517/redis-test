package com.lb.redis.controller;

import com.lb.redis.pub.sub.MsgPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : lb
 * @date : 2020/10/15 15:58
 * @description : Redis 发布订阅控制器
 */
@RestController
@RequestMapping("/pub/sub")
public class RedisPubSubController {

    @Autowired
    MsgPublisher msgPublisher;

    @PostMapping("/sendMsg")
    public String sendMsg(@RequestParam("msg") String msg) {
        msgPublisher.sendMsg(msg);
        return "发送成功";
    }

}
