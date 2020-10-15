package com.lb.redis.pub.sub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;

/**
 * @author : lb
 * @date : 2020/10/15 15:47
 * @description : 发布配置类
 */
@Configuration
public class PubConfig {

    /**
     * 订阅发布的主题
     * */
    @Bean
    ChannelTopic topic(){
        return new ChannelTopic("pubsub:queue");
    }

}
