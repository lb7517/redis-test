package com.lb.redis.component.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author : lb
 * @date : 2020/9/11 14:44
 * @description : 实例化redisTemplate类
 */
@Configuration
public class RedisTemplateFactory {

    /**
     * 作用防止入缓存乱码,使用 lettuce 实现redis线程池
     * */
    /*@Bean
    public RedisTemplate setRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        RedisSerializer redisSerializer = new StringRedisSerializer();
        //key序列化方式
        redisTemplate.setKeySerializer(redisSerializer);
        //value序列化
        redisTemplate.setValueSerializer(valueSerializer());
        //key haspmap序列化
        redisTemplate.setHashKeySerializer(redisSerializer);
        //value hashmap序列化
        redisTemplate.setHashValueSerializer(valueSerializer());
        return redisTemplate;
    }*/

    /**
     * 作用防止入缓存乱码,使用 Jedis 实现redis线程池
     * */
    @Bean
    public RedisTemplate setRedisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        RedisSerializer redisSerializer = new StringRedisSerializer();
        //key序列化方式
        redisTemplate.setKeySerializer(redisSerializer);
        //value序列化
        redisTemplate.setValueSerializer(valueSerializer1());
        //key haspmap序列化
        redisTemplate.setHashKeySerializer(redisSerializer);
        //value hashmap序列化
        redisTemplate.setHashValueSerializer(valueSerializer1());
        return redisTemplate;
    }

    /**
     * 使用Jackson序列化器，方式一
     * */
    private RedisSerializer<Object> valueSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    /**
     * 使用Jackson序列化器，方式二
     * */
    private RedisSerializer<Object> valueSerializer1() {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }

}
