package com.lb.redis.component.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : lb
 * @date : 2020/9/11 14:44
 * @description : 实例化redisTemplate类
 */
@Configuration
public class RedisTemplateFactory {

    // 哨兵
    /*@Value("${spring.redis.sentinel.master}")
    private String sentinelName;

    *//*@Value("${spring.redis.password}")
    private String password;*//*

    @Value("${spring.redis.sentinel.nodes}")
    private String[] sentinels;*/

    // 集群
    @Value("${spring.redis.cluster.nodes}")
    private String[] nodes;

    /**
     * Lettuce客户端整合(集群模式)
     * */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisClusterConfiguration rcc= new RedisClusterConfiguration();
        for (String node : nodes) {
            String[] nodes = node.split(":");
            rcc.addClusterNode(new RedisNode(nodes[0], Integer.parseInt(nodes[1])));
        }
//        rsc.setPassword(password);
        return new LettuceConnectionFactory(rcc);
    }

    /**
     * Lettuce客户端整合(哨兵模式)
     * */
   /* @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisSentinelConfiguration rsc= new RedisSentinelConfiguration();
        rsc.setMaster(sentinelName);
        List<RedisNode> redisNodeList= new ArrayList<>();
        for (String sentinel : sentinels) {
            String[] nodes = sentinel.split(":");
            redisNodeList.add(new RedisNode(nodes[0], Integer.parseInt(nodes[1])));
        }
        rsc.setSentinels(redisNodeList);
//        rsc.setPassword(password);
        return new LettuceConnectionFactory(rsc);
    }*/

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
