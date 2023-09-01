# Redis

## 单机
```shell
export REDIS_VERSION=7.0.0
export DOCKER_REDIS_NAME=redis
export REDIS_PORT=6379
# 密码
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
# FLUSHALL # 清空db
```

## 主从哨兵

```shell
# 几台从节点
export SLAVE_NUM=3
# 几台哨兵节点
export SENTINEL_NUM=3
# 密码
export REDIS_PASSWORD=chenqfredis
export REDIS_BASE_DIR=/docker/redis/sentinel/
export REDIS_NET_NAME=redis-net
export REDIS_VERSION=7.0.0
export REDIS_PORT=6379
export SENTINEL_PORT=26379
export MASTER_NAME=redis-master
export SLAVE_NAME=redis-slave
export SENTINEL_NAME=redis-sentinel
export SUBNET=192.168.1.0/24
export MASTER_IP=192.168.1.2
export VOTE_NUM=$(echo "scale=0; (${SLAVE_NUM} + 1) / 2" | bc)
docker pull redis:${REDIS_VERSION}
docker stop ${MASTER_NAME} &> /dev/null
docker rm ${MASTER_NAME} &> /dev/null
rm -rf ${REDIS_BASE_DIR}/master/conf
rm -rf ${REDIS_BASE_DIR}/master/data
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
docker run -d --name ${MASTER_NAME} --network ${REDIS_NET_NAME}  --ip ${MASTER_IP} -p ${REDIS_PORT}:6379 \
-v ${REDIS_BASE_DIR}/master/conf:/usr/local/etc/redis -v ${REDIS_BASE_DIR}/master/data:/data \
-e REDIS_PASSWORD=${REDIS_PASSWORD} redis:${REDIS_VERSION} redis-server /usr/local/etc/redis/redis.conf \
--appendonly yes --requirepass ${REDIS_PASSWORD} --protected-mode no
for ((i=1; i<=${SLAVE_NUM}; i++))
do
    docker stop ${SLAVE_NAME}${i} &> /dev/null
    docker rm ${SLAVE_NAME}${i} &> /dev/null
    rm -rf ${REDIS_BASE_DIR}/slave${i}/conf
    rm -rf ${REDIS_BASE_DIR}/slave${i}/data
    mkdir -p ${REDIS_BASE_DIR}/slave${i}/conf
    mkdir -p ${REDIS_BASE_DIR}/slave${i}/data
    ((REDIS_PORT=REDIS_PORT+1))
    echo "" > ${REDIS_BASE_DIR}/slave${i}/conf/redis.conf
    echo "requirepass ${REDIS_PASSWORD}" >> ${REDIS_BASE_DIR}/slave${i}/conf/redis.conf
    echo "appendonly yes" >> ${REDIS_BASE_DIR}/slave${i}/conf/redis.conf
    echo "#replica‐read‐only yes" >> ${REDIS_BASE_DIR}/slave${i}/conf/redis.conf
    docker run -d --name ${SLAVE_NAME}${i} --network ${REDIS_NET_NAME} \
    -v ${REDIS_BASE_DIR}/slave${i}/conf:/usr/local/etc/redis -v ${REDIS_BASE_DIR}/slave${i}/data:/data -e REDIS_PASSWORD=${REDIS_PASSWORD} -p ${REDIS_PORT}:6379 \
    redis:${REDIS_VERSION} redis-server /usr/local/etc/redis/redis.conf --appendonly yes --requirepass $REDIS_PASSWORD  \
    --replicaof ${MASTER_NAME} 6379 --protected-mode no --masterauth ${REDIS_PASSWORD}
done
for ((i=1; i<=${SENTINEL_NUM}; i++))
do
    docker stop ${SENTINEL_NAME}${i} &> /dev/null
    docker rm ${SENTINEL_NAME}${i} &> /dev/null
    rm -rf ${REDIS_BASE_DIR}/sentinel${i}/conf
    mkdir -p ${REDIS_BASE_DIR}/sentinel${i}/conf
    echo "" > ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "protected-mode no" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "sentinel monitor ${MASTER_NAME} ${MASTER_IP} 6379 ${VOTE_NUM}" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "sentinel auth-pass ${MASTER_NAME} ${REDIS_PASSWORD}" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "sentinel down-after-milliseconds ${MASTER_NAME} 10000" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    docker run -d --name ${SENTINEL_NAME}${i} --network ${REDIS_NET_NAME} -e REDIS_PASSWORD=${REDIS_PASSWORD} -p ${SENTINEL_PORT}:26379\
    -v ${REDIS_BASE_DIR}/sentinel${i}/conf:/usr/local/etc/redis redis:${REDIS_VERSION} redis-sentinel /usr/local/etc/redis/sentinel.conf --protected-mode no
    ((SENTINEL_PORT=SENTINEL_PORT+1))
done
```

## 集群部署

```shell
# 几台从节点
export MASTER_NAME=3
# 几台哨兵节点
export SLAVE_NUM=2
export ALL_NUM=$((MASTER_NAME * (SLAVE_NUM + 1)))
echo ${ALL_NUM}
# 密码
export REDIS_PASSWORD=chenqfredis
export REDIS_BASE_DIR=/docker/redis/cluster/
export REDIS_NET_NAME=redis-net
export REDIS_VERSION=7.0.0
export REDIS_PORT=6379
#export SUBNET=192.168.1.0/24
#export MASTER_IP=192.168.1.2
docker pull redis:${REDIS_VERSION}

export STR="docker exec -it redis-cluster-1 redis-cli -p ${REDIS_PORT} -a ${REDIS_PASSWORD} --cluster create --cluster-replicas ${SLAVE_NUM}"
for ((i=1; i<=${ALL_NUM}; i++))
do
    docker stop redis-cluster-${i} &> /dev/null
    docker rm redis-cluster-${i} &> /dev/null
    rm -rf ${REDIS_BASE_DIR}/redis-cluster-${i}/conf
    rm -rf ${REDIS_BASE_DIR}/redis-cluster-${i}/data
    mkdir -p ${REDIS_BASE_DIR}/redis-cluster-${i}/conf
    mkdir -p ${REDIS_BASE_DIR}/redis-cluster-${i}/data
    PORT=$((REDIS_PORT + i - 1))
    docker run -d --name redis-cluster-${i} --network ${REDIS_NET_NAME} -p ${PORT}:6379 \
-v ${REDIS_BASE_DIR}/redis-cluster-${i}/conf:/usr/local/etc/redis -v ${REDIS_BASE_DIR}/redis-cluster-${i}/data:/data \
-e REDIS_PASSWORD=${REDIS_PASSWORD} redis:${REDIS_VERSION} \
--appendonly yes --requirepass ${REDIS_PASSWORD} --masterauth ${REDIS_PASSWORD} --protected-mode no --cluster-enabled yes
  
    IP_ADDRESS=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-cluster-$i)
    export STR="$STR ${IP_ADDRESS}:${REDIS_PORT}"
done

eval $STR

# 验证
#docker exec -it redis-cluster-1 redis-cli -p 6379 -a chenqfredis  -c
#> cluster nodes
```

### 水平扩容

1. 将新增节点添加到集群中

```shell
redis-cli -a <password> --cluster add-node <prev-master-node-ip>:<prev-master-node-port> <new-master-node-ip>:<new-master-node-port>
redis-cli -a <password> --cluster add-node <prev-master-node-ip>:<prev-master-node-port> <new-slave-node-ip>:<new-slave-node-port>
```

2. 查看集群状态, 记录新主节点的ID

```shell
redis-cli -a <password> -c
# > cluster nodes
```

3. 为新主节点分配hash槽

```shell
redis-cli -a <password> --cluster reshard <prev-master-node-ip>:<prev-master-node-port>
```

输出如下:

... ...

How many slots do you want to move (from 1 to 16384)? 600

(ps:需要多少个槽移动到新的节点上，自己设置，比如600个hash槽)

What is the receiving node ID? <new-master-node-id> (2728a594a0498e98e4b83a537e19f9a0a3790f38)

(ps:把这600个hash槽移动到哪个节点上去，需要指定节点id)

Please enter all the source node IDs.

Type 'all' to use all the nodes as source nodes for the hash slots.

Type 'done' once you entered all the source nodes IDs.

Source node 1:all

(ps:输入all为从所有主节点(8001,8002,8003)中分别抽取相应的槽数指定到新节点中，抽取的总槽数为600个)

... ...

Do you want to proceed with the proposed reshard plan (yes/no)? yes

(ps:输入yes确认开始执行分片任务)

... ...

4. 给新主节点添加从节点

进入从节点, 使用集群命令将当前节点指定到新主节点下

```shell
redis-cli -a <password> -c -h <new-slave-node-ip> -p <new-slave-node-port>
# > cluster replicate <new-master-node-id> #后面这串id为新master节点id
```


### 水平缩容

2. 查看集群状态, 记录新主节点的ID, 记录新从节点ID

```shell
redis-cli -a <password> -c
# > cluster nodes
```

2. 删除新从节点

```shell
redis-cli -a <password> --cluster del-node <new-slave-node-ip>:<new-slave-node-port> <new-slave-node-id>
```

3. 将主节点的hash槽分配到其他节点

```shell
redis-cli -a <password> reshard <new-master-node-ip>:<new-master-node-port>
```

输出如下：
... ...

How many slots do you want to move (from 1 to 16384)? 600 

What is the receiving node ID? dfca1388f124dec92f394a7cc85cf98cfa02f86f

(ps:这里是需要把数据移动到哪？)

Please enter all the source node IDs.

Type 'all' to use all the nodes as source nodes for the hash slots.

Type 'done' once you entered all the source nodes IDs.

Source node 1:2728a594a0498e98e4b83a537e19f9a0a3790f38

(ps:这里是需要数据源，当前要删除的master-node-id)

Source node 2:done

(ps:这里直接输入done 开始生成迁移计划)

... ...

Do you want to proceed with the proposed reshard plan (yes/no)? Yes

(ps:这里输入yes开始迁移)

> 目前只能把master的数据迁移到一个节点上，暂时做不了平均分配功能

2. 删除新主节点

```shell
redis-cli -a <password> --cluster del-node <new-master-node-ip>:<new-master-node-port> <new-master-node-id>
```



