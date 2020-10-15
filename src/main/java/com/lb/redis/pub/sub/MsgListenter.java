package com.lb.redis.pub.sub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * @author : lb
 * @date : 2020/10/15 15:50
 * @description : 订阅者
 */
@Component
@Slf4j
public class MsgListenter implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("Message received: " + message.toString());
    }
}
