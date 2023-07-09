# Redis

## docker 安装

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

```java
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/test1")
    public Result teset1(){
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set("username","chenqf");

        return Result.success(1);
    }
    @RequestMapping("/test2")
    public Result test2(){
        ValueOperations operations = redisTemplate.opsForValue();
        return Result.success(operations.get("username"));
    }


    @RequestMapping("/test3")
    public Result teset3(){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set("user.age","20");

        return Result.success(1);
    }
    @RequestMapping("/test4")
    public Result test4(){
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        return Result.success(operations.get("use.age"));
    }
}
```

## 分布式锁