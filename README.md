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

[git地址](https://github.com/lb7517/redis-test.git)

目前还缺GenericJackson2JsonRedisSerializer和Jackson2JsonRedisSerializer区别



