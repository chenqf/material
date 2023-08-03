# Redis

> 单节点QPS可达10W+

Redis本身是多线程的, 但网络IO和键值对读写是由一个线程来完成的

Redis单线程如何处理那么多的并发客户端连接: IO多路复用

Redis的其他功能, 比如持久化/异步删除/集群同步等, 是由其他线程执行的

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

**Redis客户端命令对应的RedisTemplate中的方法列表**

![image-20230726145826513](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726145826513.png)

![image-20230726145839332](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726145839332.png)

![image-20230726145859855](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230726145859855.png)

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

### redis.confi













## 命令

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






```shell

```



## 数据结构

+ string
+ hash
  + 过期功能只能在key上不能在field上
  + 集群架构下, 不适合大规模使用
+ list
+ set
+ sort-set

### 持久化方案

TODO 

+ RDB
+ AOF
+ big key 问


6.0+ 多线程, 专机专用, 必须多核采用多线程 - 网络IO多线程, 客户端命令依然单线程
4核 配置2-3个线程
8核 配置6个线程
性能提升大致一倍

## 直接使用

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 单机部署yaml配置

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

### 主从哨兵部署yaml配置

> 主从哨兵模式下, 主节点宕机, 自动故障转移

```yaml

```

### 代码使用

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

## Redisson

## 分布式锁

TODO

## 布隆过滤器

TODO

## 哈希碰撞

rehash 是什么?