# Redis 分布式锁

## Redis 部署

[传送门](https://github.com/chenqf/material/tree/main/01.concept/redis)

## 特征

+ 排他独占
+ 防死锁 - 加锁后宕机,锁过期自动删除
+ 可重入 - 同线程其他方法加锁可成功
+ 防误删 - 只有自己加的锁才可删除
+ 原子性 - Lua脚本保证
+ 自动续期(看门狗) - 避免业务完成前锁失效

## 基于 RedisTemplate 实现

### pom.xml
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### application.yml

```yaml
server:
  port: 8009
spring:
  application:
    name: lock-redis
  redis:
    host: ${ENV_CLOUD_IP}
    port: 6379
    password: chenqfredis
```

### java 封装

```java
public class LockManager {

    private StringRedisTemplate redisTemplate;

    private String uuid; // server process id

    private long expire = 30; // seconds
    private String lockName;

    public LockManager(StringRedisTemplate redisTemplate, String uuid, String lockName) {
        this.redisTemplate = redisTemplate;
        this.uuid = uuid + Thread.currentThread().getId(); // 确保锁可重入
        this.lockName = lockName;
    }

    /**
     * 续期-看门狗
     */
    private void renewExpire(){
        String script = "if redis.call('hexists', KEYS[1], ARGV[1]) == 1 " +
                "then " +
                "return redis.call('expire', KEYS[1], ARGV[2]) " +
                "else " +
                "return 0 " +
                "end";
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList(lockName), uuid, String.valueOf(expire))) {
                    renewExpire();
                }
            }
        },expire / 3 * 1000);
    }

    public void lock() throws InterruptedException {
        String script = "if redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1 " +
                "then " +
                "redis.call('hincrby', KEYS[1], ARGV[1], 1) " +
                "redis.call('expire', KEYS[1], ARGV[2]) " +
                "return 1 " +
                "else " +
                "return 0 " +
                "end";
        while (!this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList(lockName), uuid, String.valueOf(expire))){
            Thread.sleep(50);
        }
        // 加锁成功,开启定时器自动续期
        this.renewExpire();
    }

    public void unlock(){
        String script = "if redis.call('hexists', KEYS[1], ARGV[1]) == 0 " +
                "then " +
                "return nil " +
                "elseif redis.call('hincrby', KEYS[1], ARGV[1], -1) == 0 " +
                "then " +
                "return redis.call('del',KEYS[1]) " +
                "else " +
                "return 0 " +
                "end";

        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);
        if(flag == null){
            throw new IllegalMonitorStateException("You don't have this lock!");
        }
    }
}
```

```java
@Component
public class LockClient {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String uuid;

    public LockClient() {
        // LockClient 为单例,uuid在当前服务进程内保持不变
        this.uuid = UUID.randomUUID().toString();
    }

    public LockManager getRedisLock(String lockName) {
        return new LockManager(redisTemplate, uuid, lockName);
    }
}
```

#### 使用

```java
@Service
public class DemoService {

    @Autowired
    private LockClient lockClient;

    public void test1() throws InterruptedException {
        LockManager redisLock = lockClient.getRedisLock("lock-key");
        try {
            redisLock.lock();
            // 业务逻辑-------------------
        }finally {
            redisLock.unlock();
        }
    }
}
```

## AOP + Annotation + Redisson 

### pom.xml

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```
```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
</dependency>
```

### 注册 Redisson

```java
@Configuration
public class RedissonConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host +":" + port)
                .setPassword(password)
                .setDatabase(0)
                .setConnectionMinimumIdleSize(50) // 连接池最小空闲数
                .setConnectionPoolSize(100) // 连接池最大线程数
                .setIdleConnectionTimeout(50000) // 线程超时时间
                .setConnectTimeout(20000) // 客户端获取redis连接的超时时间
                .setTimeout(10000); // 响应超时时间
        return Redisson.create(config);
    }
}
```

### AOP + Annotation

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLock4r { }
```

```java
@Component
@Aspect
@Slf4j
public class AutoLock4rAspect {

    private final String PRE_LOCK_KEY = "Distributed-lock-redis";

    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(com.maple.lock4r.annotation.AutoLock4r)")
    public void lockPointcut() {}


    @Around("lockPointcut()")
    public Object applyLock(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Signature signature = joinPoint.getSignature();
        MethodSignature msg = (MethodSignature) signature;
        Object target = joinPoint.getTarget();
        Method method = target.getClass().getMethod(msg.getName(), msg.getParameterTypes());
        String className = target.getClass().getName();
        String methodName = method.getName();

        String lockKey = PRE_LOCK_KEY + ":" + className + ":" + methodName;
        if (args.length > 0) {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(args);
            lockKey = lockKey + ":" + json;
        }
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }
    }
}
```

### 使用

```java
@Service
public class DemoService {
    
    @AutoLock4r
    public void test2() {
        // 业务逻辑-------------------
    }
}
```

