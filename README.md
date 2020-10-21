#### springboot使用redis

* * *

**注意:**

1.  使用Swagger注意要在Application中引入@EnableSwagger2注解，否则访问不了
2.  使用@Autowired方式注入RedisTemplate对象是要注意，名称要和@Bean中的方法名称对应，否者就要通过@Qualifier方式来取别名，不然会使用springboot容器默认对象
3.  application.properties中有两种方式创建redis连接池:
* 使用 lettuce 创建连接池； 使用 Jedis 创建连接池；目前springboot2.x采用lettuce创建，由于Jedis多线程情况下是线程不安全的；使用lettuce主要
4.  序列化的方式有三种:
* 默认采用 JdkSerializationRedisSerializer 序列化，这种方式缺陷明显: 首先它要求存储的对象都必须实现java.io.Serializable接口，比较笨重; 其次，他存储的为二进制数据，这对开发者是不友好的
* Jackson2JsonRedisSerializer 序列化，优点是速度快，序列化后的字符串短小精悍，不需要实现Serializable接口。
* GenericJackson2JsonRedisSerializer 序列化，这种序列化方式不用自己手动指定对象的Class。所以其实我们就可以使用一个全局通用的序列化方式了。使用起来和JdkSerializationRedisSerializer基本一样。

* * *
**redis分布式锁**

1.  使用RedisLock加锁时，可能存在多个线程并发执行redisCache.getAndSet()，那么虽然最终只有一个客户端可以加锁，但是这个客户端的锁的value可能被其他客户端覆盖, 导致锁释放不了
2.   **com.lb.redis.RedisLockTest 类中:**
*  noRedisLock()测试方式没有使用锁，是线程不安全的；
*  synchronizedLock()测试方式使用的是java synchronized锁, 只是用于单进程；
*  redisLock()测试方式使用的setnx redis指令实现(分布式锁)，使用单进程和多进程；(**1. 此处使用redis setnx指令实现； 2. 也可以使用Redission实现，这种方式可以规避服务宕机使用setnx指令加锁异常**)


* * *
**哨兵主从模式**
1. 哨兵原始配置文件: document -> sentinel.conf
2. 注意 application.properties 哨兵的配置和 RedisTemplateFactory中LettuceConnectionFactory工程的实例化bean,把哨兵注入


[git地址](https://github.com/lb7517/redis-test.git)

目前还缺GenericJackson2JsonRedisSerializer和Jackson2JsonRedisSerializer区别



