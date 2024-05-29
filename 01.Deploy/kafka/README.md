# Kafka

## 单机安装

```shell
docker network create kafka-net --driver bridge
```

```shell
docker run -d --name kafka-standalone --hostname kafka-standalone \
    -p9092:9092 \
    -e KAFKA_CFG_NODE_ID=0 \
    -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
    -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
    -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
    -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka-standalone:9093 \
    -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
    bitnami/kafka:3.6.1
# docker exec -it kafka-standalone /bin/bash
```

```shell

```




## 集群安装

```shell
wget https://archive.apache.org/dist/kafka/3.6.1/kafka_2.12-3.6.1.tgz
```
