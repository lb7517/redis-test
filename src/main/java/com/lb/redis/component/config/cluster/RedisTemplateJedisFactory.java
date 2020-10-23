package com.lb.redis.component.config.cluster;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.lb.redis.component.cache.ShareClusterRedisProperties;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author : lb
 * @date : 2020/9/11 14:44
 * @description : 实例化redisTemplate类 ，使用Jedis连接池 (redis-cluster集群模式)
 */
//@Configuration
public class RedisTemplateJedisFactory {

    @Autowired
    ShareClusterRedisProperties redisProperties;

    // 哨兵
    /*@Value("${spring.redis.sentinel.master}")
    private String sentinelName;

    *//*@Value("${spring.redis.password}")
    private String password;*//*

    @Value("${spring.redis.sentinel.nodes}")
    private String[] sentinels;*/


    /**
     * Jedis 实现连接池 方式二:
     * 注意：
     * 这里返回的JedisCluster是单例的，并且可以直接注入到其他类中去使用
     * @return
     */
    @Bean("shareJedisCluster")
    public JedisCluster getJedisCluster() {
        Set<HostAndPort> nodes = new HashSet<>();
        for (String ipPort : redisProperties.getClusterNodes()) {
            String[] ipPortPair = ipPort.split(":");
            nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
        }
        return new JedisCluster(nodes,redisProperties.getRedisTimeout(),1000,1,new GenericObjectPoolConfig());
        //需要密码连接的创建对象方式
        /*if (StringUtils.isBlank(redisProperties.getPassword())) {
            return new JedisCluster(nodes,redisProperties.getRedisTimeout(),1000,1,new GenericObjectPoolConfig());
        } else {
            return new JedisCluster(nodes,redisProperties.getRedisTimeout(),1000,1,redisProperties.getPassword() ,new GenericObjectPoolConfig());
        }*/
    }

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisProperties.getPoolMaxActive());
        poolConfig.setMaxIdle(redisProperties.getPoolMaxIdle());
        poolConfig.setMinIdle(redisProperties.getPoolMinIdle());
        poolConfig.setMaxWaitMillis(redisProperties.getPoolMaxWait());
        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
                .usePooling()
                .poolConfig(poolConfig)
                .and()
                .readTimeout(Duration.ofMillis(redisProperties.getRedisTimeout()))
                .build();
        // 集群模式
        RedisClusterConfiguration rcc= new RedisClusterConfiguration();
        for (String node : redisProperties.getClusterNodes()) {
            String[] nodes = node.split(":");
            rcc.addClusterNode(new RedisNode(nodes[0], Integer.parseInt(nodes[1])));
        }
//        rsc.setPassword(password);
        return new JedisConnectionFactory(rcc, clientConfig);
    }


    /*
     * Lettuce客户端整合(哨兵模式)
     */
    /*@Bean
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
    public RedisTemplate setRedisTemplate(JedisConnectionFactory connectionFactory) {
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
