# RabbitMQ

## 单机
```shell
# 5672 消息队列协议端口
# 15672 Web管理界面端口
export USERNAME=admin
export PASSWORD=chenqf
export VERSION=3.12.1
export DOCKER_NAME=rabbitmq1
docker run -d --name ${DOCKER_NAME} -e RABBITMQ_DEFAULT_USER=${USERNAME} -e RABBITMQ_DEFAULT_PASS=${PASSWORD} -p 15672:15672 -p 5672:5672 -p 25672:25672 -p 61613:61613 -p 1883:1883 rabbitmq:${VERSION}-management
```

## 集群部署 + 镜像模式 + HAProxy + Keepalived

> 官方说明,未来镜像模式将删除, 推荐使用 Quorum Queue

普通集群仅能提高吞吐量, 消息仅保存在一个Rabbit节点上, 不会同步到其他节点

未保证高可用, 须使用镜像模式, 将消息同步到所有Rabbit节点上

使用HAProxy作为集群的负载均衡器, 摆脱单点故障,实现故障转移

使用Keepalived防止HAProxy单点崩溃(暴露虚拟IP, HAProxy故障时进行IP漂移)

```shell
# 集群数量
export CLUSTER_NUM=3
export USERNAME=admin
export PASSWORD=chenqf
export VERSION=3.12.1
export DOCKER_NAME=rabbitmq
export DOCKER_MASTER_NAME=${DOCKER_NAME}1
export RABBIT_NET_NAME=rabbit-net
export DATA_DIR=/docker/rabbit/cluster
export P1=15672
export MIRROR_VHOST=/ # /mirror
rm -rf ${DATA_DIR}/master
mkdir -p ${DATA_DIR}/master
docker stop ${DOCKER_MASTER_NAME} &> /dev/null
docker rm ${DOCKER_MASTER_NAME} &> /dev/null
for ((i=2; i<=${CLUSTER_NUM}; i++))
do
    docker stop ${DOCKER_NAME}${i} &> /dev/null
    docker rm ${DOCKER_NAME}${i} &> /dev/null
done
docker network rm -f ${RABBIT_NET_NAME} &> /dev/null
docker network create ${RABBIT_NET_NAME}

# 启动 master
docker run -d --name ${DOCKER_MASTER_NAME} --hostname ${DOCKER_MASTER_NAME} --network ${RABBIT_NET_NAME} \
-e RABBITMQ_DEFAULT_USER=${USERNAME} -e RABBITMQ_DEFAULT_PASS=${PASSWORD} --privileged=true \
-p ${P1}:15672 -v ${DATA_DIR}/master:/var/lib/rabbitmq rabbitmq:${VERSION}-management

sleep 2s
for ((i=2; i<=${CLUSTER_NUM}; i++))
do
    rm -rf ${DATA_DIR}/${i}
    mkdir -p ${DATA_DIR}/${i}
    ((P1=P1+1))
    docker run -d --name ${DOCKER_NAME}${i} --hostname ${DOCKER_NAME}${i} --privileged=true \
     -e RABBITMQ_DEFAULT_USER=${USERNAME} -e RABBITMQ_DEFAULT_PASS=${PASSWORD} --network ${RABBIT_NET_NAME} \
     -p ${P1}:15672 -v ${DATA_DIR}/${i}:/var/lib/rabbitmq rabbitmq:${VERSION}-management
    sleep 5s
    cp ${DATA_DIR}/master/.erlang.cookie ${DATA_DIR}/${i}/.erlang.cookie
    docker stop ${DOCKER_NAME}${i} &> /dev/null
    docker start ${DOCKER_NAME}${i} &> /dev/null
    sleep 5s
    docker exec -it ${DOCKER_NAME}${i} rabbitmqctl stop_app
    docker exec -it ${DOCKER_NAME}${i} rabbitmqctl reset
    docker exec -it ${DOCKER_NAME}${i} rabbitmqctl join_cluster rabbit@${DOCKER_MASTER_NAME}
    docker exec -it ${DOCKER_NAME}${i} rabbitmqctl start_app
done
# 设置为镜像模式
docker exec -it ${DOCKER_MASTER_NAME} rabbitmqctl set_policy HA-ALL --vhost "${MIRROR_VHOST}" "^" '{"ha-mode":"all"}'
# docker exec -it ${DOCKER_MASTER_NAME} rabbitmqctl cluster_status # 查看集群情况
# 以下为 HAProxy
mkdir -p /docker/haproxy
rm -rf /docker/haproxy/*
echo "global
    log 127.0.0.1 local3 info
    maxconn 4096
    daemon
defaults
    log global
    mode tcp
    option tcplog
    option dontlognull
    retries 3
    option redispatch
    maxconn 2046
    timeout connect 5s
    timeout client 50s
    timeout server 50s
listen rabbitmq_cluster
    bind 0.0.0.0:5672
    mode tcp
    balance roundrobin" >> haproxy.cfg
for ((i=1; i<=${CLUSTER_NUM}; i++))
do
  echo "    server ${DOCKER_NAME}${i} ${DOCKER_NAME}${i}:5672 check inter 2000 rise 2 fall 2" >> haproxy.cfg
done
echo "listen monitor
    bind 0.0.0.0:8100
    mode http
    option httplog
    stats enable
    stats uri /rabbit
    stats refresh 5s" >> haproxy.cfg
docker stop haproxy &> /dev/null
docker rm haproxy &> /dev/null
docker run -d --name haproxy --network ${RABBIT_NET_NAME} -p5672:5672 -p8100:8100 -v /docker/haproxy/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg haproxy:2.8.1
#  docker exec -it -u 0 haproxy bash
# TODO Keepalived
```








