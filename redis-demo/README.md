# Redis

> 单节点QPS可达10W+

Redis本身是多线程的, 但网络IO和键值对读写是由一个线程来完成的

Redis单线程如何处理那么多的并发客户端连接: IO多路复用

Redis的其他功能, 比如持久化/异步删除/集群同步等, 是由其他线程执行的

6.0+ 多线程, 专机专用, 必须多核采用多线程 - 网络IO多线程, 客户端命令依然单线程

+ 4核 配置2-3个线程
+ 8核 配置6个线程

`Spring Boot 配置`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 持久化 - RDB

一定时间内存储一定数量的数据,会触发一次整体数据快照

### 同步快照 - save

会阻塞客户端命令

```shell
# 通过redis命令手动触发
save
```

### 异步快照 - bgsave

bgsave使用写时复制技术,子进程读取主线程的内存数据并写入RDB文件

异步快照过程中, 不阻塞客户端命令, 若有写操作, 不仅写入内存中, 还会写入RDB文件中

**按照规则自动触发的快照,使用的是bgsave**

```shell
# 通过redis命令手动触发
bgsave
```

### redis.conf

```shell
dbfilename "dump.rdb" # 持久化文件名
dir "/usr/local/redis-xxx/data/6379" # 持久化文件位置
save 60 1000 # 60秒内至少1000个键被改动, 自动保存一次数据
save 30 500 # 可配置多个, 若想禁用RDB策略, 将save注释就可
```

> 未触发RDB持久化条件前发生宕机, 会导致部分数据丢失

## 持久化 - AOF

将修改的每一条指令记录进文件appendonly.aof中(先写入os cache, 每隔一段时间fsync到磁盘)

Redis重启时, 程序重新执行AOF文件中的命令, 来达到重建数据集的目的

#### AOF重写

将aof文件中的命令进行整合, 将无效命令或重复命令进行重写, 降低aof文件体积, 提高数据恢复数据

### redis.conf

```shell
appendonly yes # 开启AOF持久化
appendfilename "appendonly.aof" # AOF持久化文件名
dir "/usr/local/redis-xxx/data/6379" # 持久化文件位置
#appendfsync always # 每触发一条命令就写入磁盘 - 最安全 - 最多丢一条命令
#appendfsync no # 速度最快,也最不安全, 交给操作系统来进行fsync 
appendfsync everysec # 每秒fsync一次,足够快,出现故障仅丢失1秒的数据 - 默认-推荐
# auto-aof-rewrite-min-size 64m # aof至少达到64M才会进行重新
# auto-aof-rewrite-percentage 100 # aof文件自上一次重写后文件大小增长了100%则再次触发重写
```

## 混合持久化 - 兼顾恢复性能和数据安全

RDB: 二进制快照文件, 恢复数据速度快
AOF: 存储的是每一条命令, 恢复数据快

**结合RDB和AOF的优点:**

在进行AOF重写的时候,将AOF中的所有命令转换为二进制备份文件存储在AOF文件中,新增加的命令再按照AOF的规则写入AOF文件

+ 可关闭RDB持久化
+ 必须开启AOF持久化

### redis.conf

```shell
aof-use-rdb-preamble yes # 开启混合持久化
```

## 备份策略

1. 写crontab定时调度脚本, 没小时copy一份aof文件到另一个目录中, 仅仅保留48小时的备份
2. 每天晚上都保留一份当日的数据备份到另一台机器上, 可保留最近1个月的备份

## 主从架构 & 哨兵

主从第一次连接:

![image-20230726125453104](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726125453104.png)

从节点断开后, 再次连接:

![image-20230726125525601](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726125525601.png)


批量同步时, master会执行bgsave生成RDB快照传输致slave (可以不开启RDB持久化)

若存在多个slave同时同步数据, 会造成master压力过大, 造成`主从复制风暴`, 可让部分从节点与从节点相连

![image-20230726125817878](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726125817878.png)

从节点 redis.conf 配置:

```shell
replicaof <master-ip> <master-port> # master节点ip+port
masterauth <master-password> # master节点密码
replica‐read‐only yes # 配置从节点只读
```

> 主从架构下, 一般作为主备, 若需要读写分离, 须单独写代码拦截redis命令, 对读写命令进行分离

单点故障后, 需要运维人员或者开发人员介入, 无法自动进行主备切换

### 哨兵模式

在主从架构的基础上增加sentinel节点, sentinel也是redis服务

sentinel节点不提供读写服务, 主要用来监控redis实例节点

sentinel节点会定期对master进行心跳检查, 当master节点宕机之后,sentinel会在slave中推选一个作为master 

![image-20230726131811204](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726131811204.png)

**sentinel节点 sentinel.conf :**

```shell
# master‐redis‐name 名字随便取, 客户端连接时会用到
# master‐redis‐ip master节点IP
# master‐redis‐port master节点PORT
# quorum 指明当有多少个sentinel认为一个master失效时才算失效 (一般值为 sentinel总数 / 2 + 1)
sentinel monitor <master‐redis‐name> <master‐redis‐ip> <master‐redis‐port> <quorum> 
sentinel auth-pass <master‐redis‐name> <password> # 指定监控的redis密码
sentinel down-after-milliseconds <master‐redis‐name> <time> # 指定心跳检测时, 多长时间没有返回代表已宕机 
```

Spring-Boot 配置:

```yaml
spring:
  redis:
    database: 0
    timeout: 3000
    sentinel:
      master: redis-master
      password: chenqfredis
      nodes: ${CLOUD.IP}:26379,${CLOUD.IP}:26380,${CLOUD.IP}:26381
```

### 哨兵的缺点

+ 哨兵选举master节点实现主从切换的过程中, Redis访问中断
+ 哨兵模式只有一个节点对外提供服务, 没法很好的并发
+ 单个主节点内存不宜设置过大(小于10G), 否则导致持久化文件过大, 影响数据恢复/主从同步效率


## 高可用集群架构

+ 部分解决选举时瞬断问题 - 选举集群无法访问,其他集群可以访问
+ 通过横向扩容, 实现存储大量数据
+ 通过横向扩容, 支持大量并发情况

![image-20230803083457170](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230803083457170.png)

> 将每个节点设置成集群模式，这种集群模式没有中 心节点，可水平扩展.
>
> 官方要求, 至少3个master节点 , 最多不超过1000个

**redis.conf**

```shell
cluster‐enabled yes #（启动集群模式）
cluster‐node‐timeout 10000 # 超时时间
requirepass chenqfrediss # redis密码
masterauth chenqfrediss  # 设置集群节点间访问密码，跟上面一致
cluster-require-full-coverage no # 某个主库下线,集群依然可用
min‐replicas‐to‐write 1 # 写数据成功最少同步的slave数量,这个数量可以模仿大于半数机制配置，比如集群总共三个节点可以配置1，加上leader就是2，超过了半数
```

Spring-Boot 配置:
```yaml
spring:
  redis:
    database: 0
    password: chenqfredis
    timeout: 3000
    cluster:
      nodes: ${ENV_CLOUD_IP}:6379,${ENV_CLOUD_IP}:6380,${ENV_CLOUD_IP}:6381,${ENV_CLOUD_IP}:6382,${ENV_CLOUD_IP}:6383,${ENV_CLOUD_IP}:6384,${ENV_CLOUD_IP}:6385,${ENV_CLOUD_IP}:6386,${ENV_CLOUD_IP}:6387
```

### 集群原理

> Redis Cluster 将所有数据划分为 16384 个 slots(槽位)，每个节点负责其中一部分槽位。槽位的信息存储于每 个节点中。

> 当 Redis Cluster 的客户端来连接集群时，它也会得到一份集群的槽位配置信息并将其缓存在客户端本地。这 样当客户端要查找某个 key 时，可以直接定位到目标节点。

> 当客户端向一个错误的节点发出了指令，该节点会发现指令的 key 所在的槽位并不归自己管理，这时它会向客 户端发送一个特殊的跳转指令携带目标操作的节点地址，告诉客户端去连这个节点去获取数据。客户端收到指令后除了跳转到正确的节点上去操作，还会同步更新纠正本地的槽位映射表缓存，后续所有 key 将使用新的槽位映射表。


### 集群选举

当slave发现自己的master变为FAIL状态时，便尝试进行Failover，以期成为新的master。由于挂掉的master 可能会有多个slave，从而存在多个slave竞争成为master节点的过程

1. slave发现自己的master变为FAIL
2. 将自己记录的集群currentEpoch加1，并广播FAILOVER_AUTH_REQUEST 信息
3. 其他节点收到该信息，只有master响应，判断请求者的合法性，并发送FAILOVER_AUTH_ACK，对每一个 epoch只发送一次ack
4. 尝试failover的slave收集master返回的FAILOVER_AUTH_ACK
5. slave收到`超过半数master的ack`后变成新Master
6. slave广播Pong消息通知其他集群节点, 告知自己变为master

> 理论上持有数据更新的slave将会首先发起选举

### 脑裂问题

由于网络分区的问题, 可能会导致脑裂问题:

1. 出现网络分区问题, slave无法连接master
2. slave判断master宕机, 发起选举
3. slave变为master - 同时存在两个master提供服务
4. 两个master的数据不一致
5. 网络分区问题恢复
6. 原master节点在网络分区状态下新增的数据全部删除
7. 原master节点变为slave

通过配置`min‐replicas‐to‐write`, 一定程度避免原master还能写入成功的问题

`min‐replicas‐to‐write:` 写数据成功最少同步的slave数量，这个数量可以模仿大于半数机制配置，比如 集群总共三个节点可以配置1，加上leader就是2，超过了半数

> 这个配置在一定程度上会影响集群的可用性，比如slave要是少于1个，这个集群就算leader正常也不能提供服务了，需要具体场景权衡选择

### 批量操作命令的支持

+ 对于类似mset，mget这样的多个key的原生批量操作命令，redis集群只支持所有key落在同一slot的情况
+ 如果有多个key一定要用mset命令在redis集群上操作，则可以在key的前面加上{XX}
+ 这样参数数据分片hash计算的只会是大括号里的值，这样能确保不同的key能落到同一slot里去

```shell
mset {user1}:1:name zhuge {user1}:1:age 18
```

假设name和age计算的hash slot值不一样，但是这条命令在集群下执行，redis只会用大括号里的 user1 做hash slot计算，所以算出来的slot值肯定相同，最后都能落在同一slot

## 非常用

+ 压测命令:

```shell
redis-benchmark <command>
```

+ 扫描key

避免keys的性能问题, 使用scan

遍历过程中, 出现新增修改删除, 可能出现: 新增的键没有遍历到, 遍历出重复键

cursor代表hash桶的值

count值并不代表遍历的结果数量, 可能多可能少

```shell
SCAN cursor match <pattern> count <number>
```

### 管道

不具有原子性, 仅是多条命令一次性发送, 减少网络消耗, 命令的执行还是单挑执行

### Lua 脚本

+ 减少网络开销, 和管道类似
+ 原子操作, 作为整体执行, 中间不会插入其他命令
+ 替代redis的事务

## 直接使用

+ opsForValue : 字符串
+ opsForList  : 列表
+ opsForSet   : 集合
+ opsForZSet  : 有序集合
+ opsForHash  : 散列

**Redis客户端命令对应的RedisTemplate中的方法列表**

![image-20230726145826513](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726145826513.png)

![image-20230726145839332](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726145839332.png)

![image-20230726145859855](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726145859855.png)

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

### 分布式锁

+ 排他独占
+ 防死锁 - 加锁后宕机,锁过期自动删除
+ 可重入 - 同线程其他方法加锁可成功
+ 防误删 - 只有自己加的锁才可删除
+ 原子性 - Lua脚本保证
+ 自动续期(看门狗) - 避免业务完成前锁失效

#### 自己实现

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

**使用**

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

#### AOP + Annotation + Redisson

注册 Redisson: 

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

AOP + Annotation:

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

    @Pointcut("@annotation(com.maple.lock4r.annotation.Lock4r)")
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

使用:

```java
@Service
public class DemoService {
    
    @AutoLock4r
    public void test2() {
        // 业务逻辑-------------------
    }
}
```



### 作为缓存



## 多数据源
TODO 

## Redisson


## 布隆过滤器

TODO

## 哈希碰撞

rehash 是什么?