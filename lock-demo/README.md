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

```

#### RedLock

一般production环境, Redis会部署 主从集群, 并配合哨兵模式.

lock ---> master ---> set ---> IO(write log) 