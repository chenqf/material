# Redis

> 单节点QPS可达10W+


## docker 安装

### 单机部署

```shell
mkdir -p /docker/redis/data
mkdir -p /docker/redis/conf
cd /docker/redis/conf
wget https://raw.githubusercontent.com/redis/redis/7.0/redis.conf
# 指定密码
echo "requirepass chenqfredis" >> redis.conf
# 开启持久化
sed -i 's/appendonly no/appendonly yes/g' redis.conf
# 开启远程访问
sed -i 's/bind 127.0.0.1/#bind 127.0.0.1/g' redis.conf
sed -i 's/protected-mode yes/protected-mode no/g' redis.conf
# 运行 redis
docker run --name redis -v /docker/redis/conf/redis.conf:/etc/redis/redis.conf -v /docker/redis/data:/data -p 6379:6379 -d redis:7.0.0 redis-server /etc/redis/redis.conf
# 访问redis
# docker exec -it redis /bin/bash
# redis-cli -h 127.0.0.1 -p 6379 -a chenqfredis
```
### 读写分离
TODO
### 哨兵模式
TODO

## 直接使用

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
server:
  port: 8003
spring:
  application:
    name: redis-demo
  redis:
    database: 0
    host: ${CLOUD.IP}
    port: 6379
    password: chenqfredis
```

+ opsForValue : 字符串
+ opsForList  : 列表
+ opsForSet   : 集合
+ opsForZSet  : 有序集合
+ opsForHash  : 散列

```java
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private RedisTemplate redisTemplate; // K,V 是 Object

    @Autowired
    private StringRedisTemplate stringRedisTemplate; // K,V 都是 String
    
    @RequestMapping("/set")
    public Result set(){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set("user.age","20");

        return Result.success(1);
    }
    @RequestMapping("/get")
    public Result get(){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        return Result.success(operations.get("use.age"));
    }
}
```

### 自定义序列化

> redis保存对象时, 默认使用JDK序列化机制
> 
> Jackson2JsonRedisSerializer将数据以json的方式缓存

```java
@Configuration
public class MyRedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(keySerializer());
        template.setHashKeySerializer(keySerializer());
        template.setValueSerializer(valueSerializer());
        template.setHashValueSerializer(valueSerializer());
        return template;
    }

    private RedisSerializer<String> keySerializer(){
        return new StringRedisSerializer();
    }

    private RedisSerializer<Object> valueSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
```

## 多数据源
TODO 

## 分布式锁

TODO

## 布隆过滤器

TODO