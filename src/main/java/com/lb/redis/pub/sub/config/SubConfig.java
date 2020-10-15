package com.lb.redis.pub.sub.config;

import com.lb.redis.pub.sub.MsgListenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author : lb
 * @date : 2020/10/15 15:53
 * @description : 订阅配置 (主要定义了RedisMessageListenerContainer 容器，
 * 并把监听器放到容器中，并添加监听器监听的主题。)
 */
@Configuration
public class SubConfig {

    @Bean
    MessageListenerAdapter messageListenerAdapter() {
        return new MessageListenerAdapter(new MsgListenter());
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(LettuceConnectionFactory factory) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(messageListenerAdapter(), new ChannelTopic("pubsub:queue"));
        return container;
    }

}
