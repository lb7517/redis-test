package com.lb.redis.component.config.cluster;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.lb.redis.component.cache.ShareClusterRedisProperties;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author : lb
 * @date : 2020/9/11 14:44
 * @description : 实例化redisTemplate类, 使用lettuce连接池  (redis-cluster集群模式)
 */
@Configuration
public class RedisTemplateLettuceFactory {

    @Autowired
    ShareClusterRedisProperties redisProperties;

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

    @Bean
    public DefaultClientResources lettuceClientResources() {
        return DefaultClientResources.create();
    }

    /**
     * Lettuce客户端整合(集群模式) 方式一:
     * */
    @Bean
    // 拓扑刷新策略 ，redis-cluster集群模式，master宕机不能切换服务问题(配合方式一使用):
    @ConditionalOnProperty(name = "spring.redis.lettuce.cluster.refresh.adaptive.enabled", havingValue = "false")
    public LettuceConnectionFactory redisConnectionFactory(RedisProperties redisProperties, ClientResources clientResources) {
        // 拓扑刷新策略 ，redis-cluster集群模式，master宕机不能切换服务问题，解决方法方式二:
        /*ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                //开启定时刷新, 按照周期刷新拓扑, 默认30s
                .enablePeriodicRefresh(Duration.ofSeconds(30))
                //开启自适应刷新, 根据事件刷新拓扑
                .enableAllAdaptiveRefreshTriggers()
                .build();

        ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
                //redis命令超时时间,超时后才会使用新的拓扑信息重新建立连接
                .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(10)))  // 暂时不知道作用，不写也可以
                .topologyRefreshOptions(topologyRefreshOptions)
                .build();

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .clientResources(clientResources) // 暂时不知道作用， 不写也可以
                .clientOptions(clusterClientOptions)
                .build();*/

        RedisClusterConfiguration rcc= new RedisClusterConfiguration();
        for (String node : nodes) {
            String[] nodes = node.split(":");
            rcc.addClusterNode(new RedisNode(nodes[0], Integer.parseInt(nodes[1])));
        }
//        rsc.setPassword(password);
//        return new LettuceConnectionFactory(rcc, clientConfiguration);
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
