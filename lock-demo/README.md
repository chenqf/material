# Lock

## 超卖案例

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>
```
```yaml
server:
  port: 8008
spring:
  application:
    name: lock-demo
  datasource:
    url: jdbc:mysql://${ENV_CLOUD_IP}:3306/distributed_lock?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```
```java
@SpringBootApplication
@MapperScan("com.maple.lock.mapper")
public class LockApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(LockApplication.class, args);
    }
}
```
```java
@Data
@TableName("stock")
public class Stock {
    private Long id;
    private String productCode;
    private String warehouse;
    private Integer count;
}
```
```java
@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}
```
```java
@Service
public class StockService {
    @Autowired
    private StockMapper stockMapper;
    
    public void deduct(){
        Stock stock = this.stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
        if(stock != null && stock.getCount() > 0){
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }
    }
}
```

## JVM本地锁

### 使用 synchronized 解决
```java
@Service
public class StockService {
    @Autowired
    private StockMapper stockMapper;

    public synchronized void synchronizedDeduct(){
        this.deduct();
    }
    
    public void deduct(){
        Stock stock = this.stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
        if(stock != null && stock.getCount() > 0){
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }
    }
}
```

### 使用 ReentrantLock 解决
```java
@Service
public class StockService {
    @Autowired
    private StockMapper stockMapper;

    private ReentrantLock lock = new ReentrantLock();
    
    public void reentrantLockDeduct(){
        lock.lock();
        try{
            this.deduct();
        }finally {
            lock.unlock();
        }
    }

    public void deduct(){
        Stock stock = this.stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
        if(stock != null && stock.getCount() > 0){
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }
    }
}
```

> 导致JVM本地锁失效的情况
> 
> + 多例模式
> + 开始DB事务
>   + 事务隔离级别大于等于 Read Committed
>   + 前一请求锁释放,事务未提交
>   + 后一请求获取锁,查询结果为前一请求未提交前的数据
> + 集群部署

## MYSQL悲观锁 pessimistic locking

> 可解决JVM本地锁失效问题

### insert/update/delete

> 在mysql中 insert/update/delete 本身就会自动加悲观锁

```java
@Mapper
public interface StockMapper extends BaseMapper<Stock> {
    @Update("update stock set count = count - #{count} where id = #{id} and count >= #{count}")
    int pessimisticSqlDeduct(@Param("id") Integer id, @Param("count") int count);
}
```
```java
@Service
public class StockService {
    @Autowired
    private StockMapper stockMapper;

    public void pessimisticSqlDeduct(){
        this.stockMapper.pessimisticSqlDeduct(1, 1);
    };
}
```

问题: 无法记录库存变化前后的状态   

### select ... for update

> select 语句中添加悲观锁


```java
@Mapper
public interface StockMapper extends BaseMapper<Stock> {
    @Select("select * from stock where id=#{id} for update")
    Stock pessimisticSelectForUpdateDeduct(Integer id);
}
```
```java
@Service
public class StockService {
    @Autowired
    private StockMapper stockMapper;

    @Transactional
    public void pessimisticSelectForUpdateDeduct(){
        Stock stock = this.stockMapper.pessimisticSelectForUpdateDeduct(1);
        if(stock != null && stock.getCount() >= 1){
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }
    };
}
```


### 悲观锁的问题

1. 锁范围问题

默认`表级锁`, 影响其他行的更新

悲观锁表级锁变行级锁:
+ 锁的查询或者更新条件必须是索引字段 
+ 锁的查询或者更新条件必须是具体值

依照以上规则, 会将满足索引列值相等的行上锁,多个索引时取交集行上锁

2. 死锁问题

对多条数据加锁时, 加锁顺序要统一

3. 操作要统一, 避免部分select for update,部分select

TODO 读写分离时, 是否还有效

## MYSQL乐观锁 optimistic locking

> CSA机制 (compare and swap)
> 
> 利用 `时间戳` 或 `version` 来实现
> 
> 不会出现`死锁`


```java
@Data
@TableName("stock")
public class Stock {
    private Long id;
    private String productCode;
    private String warehouse;
    private Integer count;
    private Integer version;
}
```
```java
package com.maple.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.maple.lock.mapper.StockMapper;
import com.maple.lock.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 陈其丰
 */
@Service
public class StockService {
    public void optimisticDeduct() throws InterruptedException {
        Stock stock = this.stockMapper.selectById(1);
        Integer version = stock.getVersion();
        stock.setCount(stock.getCount() - 1);
        stock.setVersion(version + 1);
        UpdateWrapper<Stock> updateWrapper = new UpdateWrapper<Stock>()
                .eq("id", stock.getId())
                .eq("version", version);
        int num = this.stockMapper.update(stock, updateWrapper);
        if(num == 0){
            // 可重试
            Thread.sleep(20);
            this.optimisticDeduct();
        }
    }
}

```

若需要重试,须注意以下问题:

1. 不能加@Transactional,避免mysql连接超时
2. 重试前sThread.sleep一小段时间,避免栈内存溢出

### 乐观锁的问题

1. 高并发下性能极低
2. ABA问题
   + 更新之前数据已被他人修改
3. 读写分离下, 导致乐观锁不可靠
   + 主从同步有时间间隔,可能查询的version未更新

## MYSQL锁总结

> 悲观锁(一个SQL) > 悲观锁(select for update) > 乐观锁

1. 追求极致性能, 业务简单且不需要记录数据前后变化的情况下, 使用`悲观锁(一个SQL)`

2. 写并发低(多读), 可选用`乐观锁`

3. 写并发高(多写), 可选用`悲观锁`

使用悲观锁, 一定要注意避免`死锁`问题

## 使用Redis存取库存 & Redis锁

```shell
redis-cli -h 127.0.0.1 -p 6379 -a chenqfredis
set stock 500
```
```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```
```yaml
spring:
   application:
      name: lock-demo
   redis:
      host: ${CLOUD.IP}
      port: 6379
      password: chenqfredis
```

Redis扣库存示例:
```java
@Service
public class StockService {

   @Autowired
   private StringRedisTemplate redisTemplate;

   public void redisDeduct(){
      String stock = this.redisTemplate.opsForValue().get("stock");
      if (stock != null && stock.length() != 0) {
         int count = Integer.parseInt(stock);
         if(count > 0){
            this.redisTemplate.opsForValue().set("stock", String.valueOf(--count));
         }
      }
   }
}
```

### Redis 乐观锁 optimistic locking - 不建议使用

> 性能较差, 不建议使用

+ multi: 开启事务
+ exec:  执行事务
+ watch: 监控一个或多个key值, 如果在exec执行前,key值发生变化,取消事务执行

以上三者结合使用, 实现Redis乐观锁

```java
@Service
public class StockService {

   @Autowired
   private StringRedisTemplate redisTemplate;

   public void redisOptimisticDeduct() throws InterruptedException {
      Boolean execute = this.redisTemplate.execute(new SessionCallback<Boolean>() {
         @Override
         public Boolean execute(RedisOperations operations) throws DataAccessException {
            operations.watch("stock");
            String stock = operations.opsForValue().get("stock").toString();
            if (stock != null && stock.length() != 0) {
               int count = Integer.parseInt(stock);
               if (count > 0) {
                  operations.multi();
                  operations.opsForValue().set("stock", String.valueOf(--count));
                  List exec = operations.exec();
                  // 如果事务的返回值为空, 在代表减库存失败,重试
                  return exec != null && exec.size() >0;
               }
            }
            return true;
         }
      });
      if(!execute){
         Thread.sleep(20);
         this.redisOptimisticDeduct();
      }
   }
}
```

### Redis分布式锁

> 常见场景: 超卖 / 缓存击穿

> 特征
> 
> + 排他独占
> + 防死锁
>   - 加锁后宕机,锁过期删除
>   - 可重入, 同线程其他方法加锁可成功
> + 防误删
>   - 只有自己加的锁才可删除
> + 原子性
>   + 加锁及设置过期时间之间
>   + 判断是否是自己的锁和解锁之间
> + 自动续期
>   + 避免业务执行完成前锁失效, 被他人获取锁

#### 最简单实现

```java
@Service
public class StockService {

   @Autowired
   private StringRedisTemplate redisTemplate;

   public void redisDistributedLockDeduct() throws InterruptedException {
      String uuid = UUID.randomUUID().toString();
      String lockKey = "lock";
      // 加锁防止死锁(加锁后宕机): 设置过期时间
      // 业务代码执行时间长, 超过expire, expire没有合理续期
      while (Boolean.FALSE.equals(this.redisTemplate.opsForValue().setIfAbsent(lockKey, uuid,10, TimeUnit.SECONDS))) {
         Thread.sleep(50);
      }
      try{
         this.redisDeduct(); // 核心业务
      }finally {
         // 解锁 - 只能解自己的锁 - 未满足原子性
         if (StringUtils.equals(this.redisTemplate.opsForValue().get(lockKey),uuid)) {
            this.redisTemplate.delete(lockKey);
         }
      }
   }
}
```

> 仅满足了`排他独占`特性, 其他特征未满足

#### 进阶方案

+ Lua脚本满足原子性
+ 加锁后开启新线程续期
+ UUID结合线程ID确保只能对自己的锁解锁
+ 通过线程ID确保可重入

```java
public class RedisDistributedLock implements Lock {

    private StringRedisTemplate redisTemplate;
    private String lockName;

    private String uuid; // server process id

    private long expire = 30; // seconds

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockName, String serverUuid) {
        this.redisTemplate = redisTemplate;
        this.lockName = lockName;
        this.uuid = serverUuid + ":" + Thread.currentThread().getId();
    }

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

    @Override
    public void lock() {
        this.tryLock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        try {
            return this.tryLock(-1L,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, @NotNull TimeUnit unit) throws InterruptedException {
        if(time != -1L){
            this.expire = unit.toSeconds(time);
        }
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
        return true;
    }

    @Override
    public void unlock() {
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

    @NotNull
    @Override
    public Condition newCondition() {
        return null;
    }
}
```
```java
@Component
public class DistributedLockClient {
   @Autowired
   private StringRedisTemplate redisTemplate;

   private String uuid;

   public DistributedLockClient() {
      this.uuid = UUID.randomUUID().toString();
   }

   public RedisDistributedLock getRedisLock(String lockName){
      return new RedisDistributedLock(redisTemplate, lockName, uuid);
   }
}
```
```java
@Service
public class StockService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLockClient distributedLockClient;
    
    public void redisDeduct(){
        String stock = this.redisTemplate.opsForValue().get("stock");
        if (stock != null && stock.length() != 0) {
            int count = Integer.parseInt(stock);
            if(count > 0){
                this.redisTemplate.opsForValue().set("stock", String.valueOf(--count));
            }
        }
    }
    
    public void finalRedisDistributedLockDeduct() {
        RedisDistributedLock redisLock = this.distributedLockClient.getRedisLock("lock-demo");
        redisLock.lock();
        try {
            this.redisDeduct();
        } finally {
            redisLock.unlock();
        }
    }
}
```

##### 遗留问题

> 一般production环境, Redis会部署 主从,哨兵,集群.

1. A用户-加锁时更新master-redis
2. master-redis准备将数据同步至slave-redis
3. master-redis在同步前宕机
4. slave-redis升任为master(此时并有之前的加锁数据)
5. A用户-业务执行中,并未释放锁 / B用户-请求加锁, 加锁成功
6. 同时有2个用户获取到锁

#### RedLock解决集群问题

部署多台Redis节点, 彼此隔离相互无主从关系

1. client获取系统时间
2. client使用相同的KV值依次从多个Redis中获取锁,且指定超时时间
   + 若某个节点超过一定时间依然加锁失败,直接放弃
   + 避免被宕机节点阻塞
   + 尽快尝试到下一节点加锁
3. 计算获取锁的消耗时间 = 加锁前的系统时间 - step1中的时间
   1. 获取锁的消耗时间小于总锁定时间
   2. 半数以上节点加锁成功
   + 满足以上条件, 认为加锁成功
4. 计算剩余锁定时间 = 总的锁定时间 - step3中的消耗时间
5. 若step3失败, 即获取锁失败, 针对所有节点释放锁

> 性能较差, 基础设施费用高, 实现复杂, 很少用

### Redisson

[Redisson文档](https://github.com/redisson/redisson/wiki)

```xml
<dependency>
   <groupId>org.redisson</groupId>
   <artifactId>redisson</artifactId>
   <version>3.20.0</version>
</dependency>
```

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
               .setDatabase(1)
               .setConnectionMinimumIdleSize(50) // 连接池最小空闲数
               .setConnectionPoolSize(100) // 连接池最大线程数
               .setIdleConnectionTimeout(50000) // 线程超时时间
               .setConnectTimeout(20000) // 客户端获取redis连接的超时时间
               .setTimeout(10000); // 响应超时时间
       return Redisson.create(config);
    }
}
```

#### 公平锁 & 非公平锁

+ 非公平锁: 先请求一次锁,拿到就拿到, 拿不到就排队等待(可能永远也拿不到了)
+ 公平锁: 先到的排在前面-依次等待

```java
@Service
public class RedissonService {
    @Autowired
    private RedissonClient redissonClient;

    public void redissonDeduct(){
        RLock lock = this.redissonClient.getLock("lock-key"); // 非公平锁
//        RLock lock = this.redissonClient.getFairLock("lock-key"); // 公平锁    
        lock.lock();
        try{
            this.deduct();
        }finally {
            lock.unlock();
        }
    }
}
```

#### 分布式-读写锁

+ 写和写不可并发
+ 读和写不可并发
+ 读和读可以并发

```java
@Service
public class RedissonService {
    @Autowired
    private RedissonClient redissonClient;

   public void testWriteLock(){
      RReadWriteLock lock = this.redissonClient.getReadWriteLock("rwLock");
      lock.writeLock().lock();
        //......read
      lock.writeLock().unlock();
   }

   public void testReadLock(){
      RReadWriteLock lock = this.redissonClient.getReadWriteLock("rwLock");
      lock.readLock().lock();
      //......write
      lock.readLock().unlock();
   }
}
```

#### 分布式-信号量

```java
@Service
public class RedissonService {
    @Autowired
    private RedissonClient redissonClient;

   public void testSemaphore(){
      RSemaphore semaphore = this.redissonClient.getSemaphore("semaphore");
      semaphore.trySetPermits(3); // 设置资源量 限流的线程数
      try {
         semaphore.acquire(); // 获取资源, 获取成功的线程继续执行,否则被阻塞
         System.out.println("执行业务.....");
         TimeUnit.SECONDS.sleep(10 + new Random().nextInt(10));
         System.out.println("业务执行完....");
         semaphore.release();
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
}
```

#### 分布式-闭锁 CountDownLatch

```java
@Service
public class RedissonService {
    @Autowired
    private RedissonClient redissonClient;

   public void testLatch(){
      RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("testCountDownLatch");
      System.out.println("我要锁门了, 还剩5个人, 你们快走...");
      countDownLatch.trySetCount(5);
      try {
         countDownLatch.await();
         System.out.println("锁上门了....");
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
   public void testCountDown(){
      RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("testCountDownLatch");
      System.out.println("我要出门了....");
      countDownLatch.countDown();
   }
}
```


## Zookeeper分布式锁

### ZK节点类型

+ 永久节点
  + create <dir> <content>
+ 临时节点 - 客户端断开连接后消失
  + create -e <dir> <content>
+ 永久序列化节点 - 可创建同名节点,实际存储添加编号实现
  + create -s <dir> <content>
+ 临时序列化节点 - 可创建同名节点,实际存储添加编号实现 - 客户端断开连接后消失
  + create -s -e <dir> <content>

+ 获取节点内容
  + get <dir>

### ZK节点的时间监听

> 所有监听都是一次性的, all client 只要监听都会收到 message

+ 节点创建 : NodeCreated
  + stat -w <dir>
+ 节点删除 : NodeDeleted
  + stat -w <dir>
+ 节点数据变化 : NodeDataChanged
  + get -w <dir>
+ 子节点变化 : NodeChildrenChanged
  + ls -w <dir>

### 分布式锁

+ 排他性 - 利用ZK节点不能重复
+ 阻塞锁 - 临时序列化节点 + 监听前一节点变化 + 闭锁
  + 公平锁
+ 可重入 - ThreadLocal实现

```xml
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
</dependency>
```
```java
@Slf4j
@Component
public class ZookeeperClient {

    @Value("${ENV_CLOUD_IP}")
    private String host;

    private ZooKeeper zookeeper = null;

    public ZooKeeper getZookeeper() {
        return zookeeper;
    }

    @PostConstruct
    public void init() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            zookeeper = new ZooKeeper(host + ":2181", 30000,event -> {
                Watcher.Event.KeeperState state = event.getState();
                if (state.equals(Watcher.Event.KeeperState.SyncConnected)) {
                    log.info("获取到zk连接....");
                    latch.countDown();
                } else if (state.equals(Watcher.Event.KeeperState.Closed)) {
                    log.info("关闭了zk连接....");
                }
            });
            latch.await();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy(){
        try {
            this.zookeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
```java
@Component
public class DistributedLockClient {
    
    @Autowired
    private ZookeeperClient zookeeperClient;

    public ZookeeperDistributedLock getZookeeperLock(String lockName){
        return new ZookeeperDistributedLock(zookeeperClient.getZookeeper(),lockName);
    }
}
```
```java
@Service
public class ZookeeperService {

    @Autowired
    private DistributedLockClient distributedLockClient;

    @Autowired
    private StockService stockService;

    public void deduct(){
        ZookeeperDistributedLock lock = distributedLockClient.getZookeeperLock("lock");
        lock.lock();
        this.stockService.redisDeduct();
        lock.unlock();
    }

}
```


> Redis分布式锁有的功能 Zookeeper分布式锁都有
> 
> Redis 实现阻塞锁很麻烦
> 
> ZK集群趋向于一致性集群, Redis集群的问题, ZK几乎不会出现

### curator

TODO

## 注解实现分布式锁 Redisson



## 总结

性能: redis > zk
可靠性: zk > redis





