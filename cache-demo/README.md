# Cache

## 直接使用

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

[docker安装redis](../redis-demo)

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

## 缓存问题

### 缓存穿透

### 缓存雪崩

### 缓存击穿

> 某个Key过期, 同一时间, 高并发访问, 所有请求直达DB