# CAP

## 一致性 Consistency

所有节点访问`同一份``最新`的数据副本

`副本一致性`, 即所有副本给出的结果都一致

## 可用性 Availability

每次请求都能获取到`非错`的响应 -- 但不保证数据为最新





## 分区容错性 Partition Tolerance

尽管任意数量的消息被节点之间的网络流失或延时, 但系统不能崩溃

## 集群架构

+ 高可用性
  + 服务可用性: 允许有服务节点停止服务
  + 数据可用性: 部分节点丢失, 不会丢失数据
+ 可扩展性
  + 请求量提升/数据量提升(将数据分布到所有节点上)


如何查看性能瓶颈, 各个组件之间 redis zookeeper nacos 等


https://www.bilibili.com/video/BV1Rb4y1W7CD/?vd_source=0494972cf815a7a4a8ac831a4c0a1229

https://www.bilibili.com/video/BV1Th41187yc/?spm_id_from=333.337.search-card.all.click&vd_source=0494972cf815a7a4a8ac831a4c0a1229





ln -s /opt/bin/redis.sh /usr/local/bin/redis

#!/bin/bash
docker exec -it redis-master redis-cli -p 6379 -a chenqfredis


#!/bin/bash
docker exec -it zookeeper ./bin/zkCli.sh -Djava.security.auth.login.config=/conf/zoo_jaas.conf



## k8s

helm charts
helm museum

@MethodExporter
@Retry
防重复提交
幂等性前置校验

spring-retry
