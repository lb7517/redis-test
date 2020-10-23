package com.lb.redis.component.cache;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : lb
 * @date : 2020/10/23 10:59
 * @description : 配置文件中的配置项
 */
@Component
@Data
public class ShareClusterRedisProperties {
    @Value("${spring.redis.timeout}")
    private Integer redisTimeout;
    @Value("${spring.redis.Jedis.pool.max-active}")
    private Integer poolMaxActive;
    @Value("${spring.redis.Jedis.pool.max-idle}")
    private Integer poolMaxIdle;
    @Value("${spring.redis.Jedis.pool.min-idle}")
    private Integer poolMinIdle;
    @Value("${spring.redis.Jedis.pool.max-wait}")
    private Integer poolMaxWait;
    @Value("${spring.redis.cluster.nodes}")
    private List<String> clusterNodes;
//    @Value("${spring.share.redis.cluster.max-redirects}")
//    private Integer clusterMaxRedirects;
//    @Value("${spring.share.redis.password}")
//    private String password;
}
