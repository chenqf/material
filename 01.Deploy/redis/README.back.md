

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
export PUBLIC_IP=121.36.70.23
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
    --replicaof ${PUBLIC_IP} 6379 --protected-mode no --masterauth ${REDIS_PASSWORD}
done
for ((i=1; i<=${SENTINEL_NUM}; i++))
do
    docker stop ${SENTINEL_NAME}${i} &> /dev/null
    docker rm ${SENTINEL_NAME}${i} &> /dev/null
    rm -rf ${REDIS_BASE_DIR}/sentinel${i}/conf
    mkdir -p ${REDIS_BASE_DIR}/sentinel${i}/conf
    echo "" > ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "protected-mode no" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "sentinel monitor ${MASTER_NAME} ${PUBLIC_IP} 6379 ${VOTE_NUM}" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "sentinel auth-pass ${MASTER_NAME} ${REDIS_PASSWORD}" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    echo "sentinel down-after-milliseconds ${MASTER_NAME} 10000" >> ${REDIS_BASE_DIR}/sentinel${i}/conf/sentinel.conf
    docker run -d --name ${SENTINEL_NAME}${i} --network ${REDIS_NET_NAME} -e REDIS_PASSWORD=${REDIS_PASSWORD} -p ${SENTINEL_PORT}:26379\
    -v ${REDIS_BASE_DIR}/sentinel${i}/conf:/usr/local/etc/redis redis:${REDIS_VERSION} redis-sentinel /usr/local/etc/redis/sentinel.conf --protected-mode no
    ((SENTINEL_PORT=SENTINEL_PORT+1))
done
```