# Redis

> 单节点QPS可达10W+


## docker 安装

### 单机部署

```shell
export REDIS_VERSION=7.0.0
export DOCKER_REDIS_NAME=redis
export REDIS_PORT=6379
export REDIS_PASSWORD=chenqfredis
export REDIS_DATA_DIR=/docker/redis/standalone/data
export REDIS_CONF_DIR=/docker/redis/standalone/conf
docker pull ${REDIS_VERSION}
mkdir -p ${REDIS_DATA_DIR}
mkdir -p ${REDIS_CONF_DIR}
cd ${REDIS_CONF_DIR}
rm -rf ${REDIS_CONF_DIR}/*
# redis 7.0
wget https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/config/redis.conf
# 指定密码
echo "requirepass ${REDIS_PASSWORD}" >> redis.conf
# 开启持久化
sed -i 's/appendonly no/appendonly yes/g' redis.conf
# 开启远程访问
sed -i 's/bind 127.0.0.1/#bind 127.0.0.1/g' redis.conf
sed -i 's/protected-mode yes/protected-mode no/g' redis.conf
# 运行 redis
docker run --name ${DOCKER_REDIS_NAME} -v ${REDIS_CONF_DIR}/redis.conf:/etc/redis/redis.conf -v ${REDIS_DATA_DIR}:/data -e REDIS_PASSWORD=${REDIS_PASSWORD} -p ${REDIS_PORT}:6379 -d redis:7.0.0 redis-server /etc/redis/redis.conf
docker ps -a | grep ${DOCKER_REDIS_NAME}
# 访问redis
# docker exec -it <container-name> /bin/bash;
# redis-cli -p 6379 -a ${REDIS_PASSWORD}
# docker exec -it <container-name> redis-cli -p 6379 -a ${REDIS_PASSWORD}
```

### 主从哨兵部署(一主N从N哨兵)

部署脚本:

```shell
# 几台从节点
export SLAVE_NUM=2
# 几台哨兵节点
export SENTINEL_NUM=3
export REDIS_PASSWORD=chenqfredis
export REDIS_BASE_DIR=/docker/redis/sentinel/
export REDIS_NET_NAME=redis-net
export REDIS_VERSION=7.0.0
export MASTER_PORT=6379
export MASTER_NAME=redis-master
export SLAVE_NAME=redis-slave
export SENTINEL_NAME=redis-sentinel
export SUBNET=192.168.1.0/24
export MASTER_IP=192.168.1.2
export VOTE_NUM=$(echo "scale=0; (${SLAVE_NUM} + 1) / 2" | bc)
docker pull redis:${REDIS_VERSION}
docker stop ${MASTER_NAME} &> /dev/null
docker rm ${MASTER_NAME} &> /dev/null
#rm -rf ${REDIS_BASE_DIR}/master/conf
#rm -rf ${REDIS_BASE_DIR}/master/data
mkdir -p ${REDIS_BASE_DIR}/master/conf
mkdir -p ${REDIS_BASE_DIR}/master/data
echo "" > ${REDIS_BASE_DIR}/master/conf/redis.conf
echo "requirepass ${REDIS_PASSWORD}" >> ${REDIS_BASE_DIR}/master/conf/redis.conf
echo "appendonly yes" >> ${REDIS_BASE_DIR}/master/conf/redis.conf
for ((i=1; i<=${SLAVE_NUM}; i++))
do
    docker stop ${SLAVE_NAME}${i} &> /dev/null
    docker rm ${SLAVE_NAME}${i} &> /dev/null
done
for ((i=1; i<=${SENTINEL_NUM}; i++))
do
  docker stop ${SENTINEL_NAME}${i} &> /dev/null
  docker rm ${SENTINEL_NAME}${i} &> /dev/null
done
docker network rm -f ${REDIS_NET_NAME} 
docker network create ${REDIS_NET_NAME} --subnet=${SUBNET}
# start master
docker run -d --name ${MASTER_NAME} --network ${REDIS_NET_NAME}  --ip ${MASTER_IP} \
-p ${MASTER_PORT}:6379 \
-v ${REDIS_BASE_DIR}/master/conf:/usr/local/etc/redis -v ${REDIS_BASE_DIR}/master/data:/data \
-e REDIS_PASSWORD=${REDIS_PASSWORD} redis:${REDIS_VERSION} redis-server /usr/local/etc/redis/redis.conf \
--appendonly yes --requirepass ${REDIS_PASSWORD} --protected-mode no
for ((i=1; i<=${SLAVE_NUM}; i++))
do
    docker stop ${SLAVE_NAME}${i} &> /dev/null
    docker rm ${SLAVE_NAME}${i} &> /dev/null
#    rm -rf ${REDIS_BASE_DIR}/slave${i}/conf
#    rm -rf ${REDIS_BASE_DIR}/slave${i}/data
    mkdir -p ${REDIS_BASE_DIR}/slave${i}/conf
    mkdir -p ${REDIS_BASE_DIR}/slave${i}/data
    echo "" > ${REDIS_BASE_DIR}/slave${i}/conf/redis.conf
    echo "requirepass ${REDIS_PASSWORD}" >> ${REDIS_BASE_DIR}/slave${i}/conf/redis.conf
    echo "appendonly yes" >> ${REDIS_BASE_DIR}/slave${i}/conf/redis.conf
    docker run -d --name ${SLAVE_NAME}${i} --network ${REDIS_NET_NAME} \
    -v ${REDIS_BASE_DIR}/slave${i}/conf:/usr/local/etc/redis -v ${REDIS_BASE_DIR}/slave${i}/data:/data -e REDIS_PASSWORD=${REDIS_PASSWORD} redis:${REDIS_VERSION} \
    redis-server /usr/local/etc/redis/redis.conf --appendonly yes --requirepass $REDIS_PASSWORD \
    --replicaof ${MASTER_NAME} 6379 --protected-mode no --masterauth ${REDIS_PASSWORD}
done
for ((i=1; i<=${SENTINEL_NUM}; i++))
do
    docker stop ${SENTINEL_NAME}${i} &> /dev/null
    docker rm ${SENTINEL_NAME}${i} &> /dev/null
#    rm -rf ${REDIS_BASE_DIR}/sentinel${i}/conf
    mkdir -p ${REDIS_BASE_DIR}/sentinel${i}/conf
    echo "" > ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "protected-mode no" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "sentinel monitor ${MASTER_NAME} ${MASTER_IP} 6379 ${VOTE_NUM}" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "sentinel auth-pass ${MASTER_NAME} ${REDIS_PASSWORD}" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "sentinel down-after-milliseconds ${MASTER_NAME} 10000" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    docker run -d --name ${SENTINEL_NAME}${i} --network ${REDIS_NET_NAME} -e REDIS_PASSWORD=${REDIS_PASSWORD} \
    -v ${REDIS_BASE_DIR}/sentinel${i}/conf:/usr/local/etc/redis redis:${REDIS_VERSION} redis-sentinel /usr/local/etc/redis/sentinel.conf --protected-mode no
done
```

```shell
# 验证哨兵部署成功
docker exec -it redis-sentinel1 redis-cli -p 26379
```

### 持久化方案

TODO 

+ RDB
+ AOF

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

## 分布式锁

TODO

## 布隆过滤器

TODO