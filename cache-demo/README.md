# Cache

多级缓存结构:

![image-20230803114106557](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230803114106557.png)

## 缓存设计

### 缓存穿透

缓存穿透是指查询一个根本不存在的数据， 缓存层和存储层都不会命中

缓存穿透将导致不存在的数据每次请求都要到存储层去查询， 失去了缓存保护后端存储的意义

原因:

1. 自身业务代码或者数据出现问题
2. 一些恶意攻击、 爬虫等造成大量空命中

解决办法:

1. 缓存空对象

2. 布隆过滤器
   + 当它说某个值存在时，这个值可 能不存在
   + 当它说不存在时，那就肯定不存在

### 缓存击穿(失效)

由于大批量缓存在同一时间失效可能导致大量请求同时穿透缓存直达数据库，可能会造成数据库瞬间压力过大甚至挂掉

对于这种情况我们在批量增加缓存时最好将这一批数据的缓存过期时间设置为一个时间段内的不同时间

### 缓存雪崩

缓存雪崩指的是缓存层支撑不住或宕掉后， 流量会像奔逃的野牛一样， 打向后端存储层。

由于缓存层承载着大量请求，有效地保护了存储层，但是如果缓存层由于某些原因不能提供服务(比如超大并发过来，缓存层支撑不住，或者由于缓存设计不好，类似大量请求访问bigkey，导致缓存能支撑的并发急剧下 降)， 于是大量请求都会打到存储层， 存储层的调用量会暴增， 造成存储层也会级联宕机的情况。

1. 保证缓存层服务高可用性，比如使用Redis Sentinel或Redis Cluster
2. 依赖隔离组件为后端限流熔断并降级。比如使用Sentinel或Hystrix限流降级组件。
3. 提前演练。 在项目上线前， 演练缓存层宕掉后， 应用以及后端的负载情况以及可能出现的问题

### 热点缓存Key重建优化

在缓存失效的瞬间， 有大量线程来重建缓存， 造成后端负载加大， 甚至可能会让应用崩溃

+ 使用分布式锁, 保证缓存重建前, 只有一个线程在访问DB

### 缓存与数据库双写不一致

1. 双写不一致

![image-20230803123512399](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230803123512399.png)

2. 读写并发不一致

![image-20230803123529638](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230803123529638.png)

解决办法:

1. 并发几率小的数据(订单数据, 用户数据等), 忽略此问题
2. 业务可容忍的情况下, 加上过期时间
3. 添加分布式读写锁(读读的时候, 相当于无锁)
4. 使用canal监听数据库的binlog日志及时的修改缓存

总结:

1. 读多写少, 基于以上规则
2. 写多读多的情况下
   1. 直接操作数据库
   2. 将redis作为主存储, 异步将数据同步到数据库, 数据库只是作为数据的备份

### key名设计

建议:

```shell
trade:order:id # 业务名:表明:id
```

## 多级缓存

TODO

## Spirng-Boot使用缓存注解

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

```java
@SpringBootApplication
@EnableCaching // 开启缓存
public class CacheApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(CacheApplication.class, args);
    }
}
```

### @Cacheable

1. 方法调用之前, 先根据value查找Cache,第一次获取不到就创建
   + value 不可为空
2. 去value对应的Cache中根据key查找entry, key使用方法中的参数来生成
   + 去value对应的Cache中根据key查找entry, key使用方法中的参数来生成
     + 无参数, key = new SimpleKey();
     + 1个参数, key = 参数的值
     + n个参数, key = new SimpleKey(params);
3. 如果查到, 直接返回, 没有查到就调用目标方法
4. 将目标方法返回的结果放到value对应的Cache中

```java
@Service
public class DemoService {
    @Cacheable(value = "cacheName1")
    public String testCache1(){
        System.out.println("enter testCache1 method");
        return new Date().toString();
    }
}
```

#### 自定义key, 使用 SpEL 表达式
> 使用 key, 则不能使用 keyGenerator

```java
@Service
public class DemoService {
    @Cacheable(value = "cacheName1",key = "#root.methodName")
    public String testCache2(){
        System.out.println("enter testCache2 method");
        return new Date().toString();
    }
}
```

#### 自定义keyGenerator
> 使用 keyGenerator, 则不能使用 key

```java
@Configuration
public class MyCacheConfig {
    @Bean("myKeyGenerator")
    public KeyGenerator keyGenerator(){
        return new KeyGenerator(){
            @Override
            public Object generate(Object target, Method method, Object... params) {
                return method.getName() + "[" + Arrays.asList(params) + "]";
            }
        };
    }
}
```
```java
@Service
public class DemoService {
    @Cacheable(value = "cacheName1",keyGenerator = "myKeyGenerator")
    public String testCache3(){
        System.out.println("enter testCache3 method");
        return new Date().toString();
    }
}
```

#### condition 指定符合条件的情况下才缓存

```java
@Service
public class DemoService {
    @Cacheable(value = "cacheName1", condition = "#a0 >1")
    public String testCache4(){
        System.out.println("enter testCache4 method");
        return new Date().toString();
    }
}
```

#### unless 指定符合条件的情况下不缓存

> unless 和 condition 正好相反

```java
@Service
public class DemoService {
    @Cacheable(value = "cacheName1", unless = "#a0 >1")
    public String testCache5(){
        System.out.println("enter testCache5 method");
        return new Date().toString();
    }
}
```

### @CachePut

> 即调用方法, 又更新缓存

1. 先调用目标方法
2. 将目标方法的返回值缓存起来

```java
@Service
public class EmployeeService {

    @Cacheable(value = "emp",key = "#id")
    public Employee getById(Integer id){
        System.out.println("method: getById");
        return this.findById(id);
    }

    @CachePut(value = "emp",key = "#id")
    public Employee updateById(Integer id, String name){
        System.out.println("method: updateById");
        Employee employee = this.findById(id);
        employee.setName(name);
        return employee;
    }


    public Employee findById(Integer id){
        Employee[] array = new Employee[]{
                new Employee(1,"a"),
                new Employee(2,"b"),
                new Employee(3,"c"),
                new Employee(4,"d"),
                new Employee(5,"e"),
        };
        for (Employee employee : array) {
            if(employee.getId().equals(id)){
                return employee;
            }
        }
        return null;
    };
}
```

### @CacheEvict

> 清除缓存

```java
@Service
public class EmployeeService {
    @CacheEvict(value = "emp", key = "#id")
    public void deleteById(Integer id){
        System.out.println("delete id: " + id);
    }
}
```

+ @CacheEvict(value="emp", allEntries=true) 
  + 删除value下的所有缓存,默认false
+ @CacheEvict(value="emp", beforeInvocation=true) 
  + 是否在方法执行前就清空缓存, 默认false

### @CacheConfig 抽取公共规则
```java
@CacheConfig(cacheNames = "emp")
@Service
public class EmployeeService {

    @Cacheable(key = "#id")
    public Employee getById(Integer id){
        System.out.println("method: getById");
        return this.findById(id);
    }

    @CachePut(key = "#id")
    public Employee updateById(Integer id, String name){
        System.out.println("method: updateById");
        Employee employee = this.findById(id);
        employee.setName(name);
        return employee;
    }

    @CacheEvict( key = "#id")
    public void deleteById(Integer id){
        System.out.println("delete id: " + id);
    }
}
```

## 使用redis作为缓存

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
spring:
  application:
    name: cache-demo
  redis:
    database: 0
    host: ${CLOUD.IP}
    port: 6379
    password: chenqfredis
```

```java
@Configuration
public class MyRedisConfig {

    private Duration timeToLive = Duration.ofHours(1);
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory){
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(timeToLive)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer()))
                .disableCachingNullValues();
        
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(configuration).transactionAware().build();
        return redisCacheManager;
    }

    private RedisSerializer<String> keySerializer(){
        return new StringRedisSerializer();
    }

    private RedisSerializer<Object> valueSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
```
