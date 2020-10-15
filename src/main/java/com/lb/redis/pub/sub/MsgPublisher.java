package com.lb.redis.pub.sub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * @author : lb
 * @date : 2020/10/15 15:44
 * @description : 发布消息类
 */
@Component
@Slf4j
public class MsgPublisher {

    @Autowired
    @Qualifier("setRedisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private ChannelTopic topic;

    public void sendMsg(String msg){
        redisTemplate.convertAndSend(topic.getTopic(), "message: " + msg
                + ";Time: " + Calendar.getInstance().getTime());
    }

}
