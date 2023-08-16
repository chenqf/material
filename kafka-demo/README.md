# Kafka

> 设计上允许少量消息丢失(大数据场景下), 通常用在允许少量数据丢失的情况, 多个版本迭代, 目前基本认为不会丢消息
> 
> 金融场景下不要使用Kafka, 建议使用 RocketMq

> Kafka在企业级应用中被广泛应用，包括实时流处理、日志聚合、监控和数据分析等方面。同时，Kafka还可以与其他大数据工具集成，如Hadoop、Spark和Storm等，构建一个完整的数据处理生态系统。

## 典型应用场景

一个典型的日志聚合的应用场景:

![image-20230816152614944](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230816152614944.png)

+ `数据吞吐量很大`:  可手机海量日志
+ `集群容错性高`: 允许集群中少量节点崩溃
+ `功能不是太复杂`: 未支持死信队列/顺序消息等 `(特殊处理)`
+ `允许少量数据丢失`: Kafka本身也在不端优化数据安全问题, 目前基本可以认为`不会丢消息`

## 设计目标

+ 高吞吐率: 单机支持每秒100万条消息的读写
+ 消息持久化: 所有消息均被持久化到磁盘, 无消息丢失, 支持`消息重放`
+ 完全分步式: Producer, Broker, Consumer 均支持水平扩展
+ 同时适应在线流处理和离线批处理

## 常见应用场景

+ 日志收集
+ 消息系统
+ 用户活动跟踪
+ 运营指标
+ 流式处理

## 简单收发消息

![image-20230816153616472](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230816153616472.png)

```shell
# 查看所有topic
kafka-topics.sh --list  --bootstrap-server kafka-standalone:9092
# 创建topic
kafka-topics.sh --create --topic test --bootstrap-server kafka-standalone:9092
```

```shell
# 作为Producer
kafka-console-producer.sh --bootstrap-server kafka-standalone:9092 --topic test

# 作为Consumer - 只能接收监听后生产的消息 - 每个消费者都会收到消息
kafka-console-consumer.sh --bootstrap-server kafka-standalone:9092 --topic test
# 作为Consumer - 从头接收消息
kafka-console-consumer.sh --bootstrap-server kafka-standalone:9092 --from-beginning --topic test
# 作为Consumer - 从0分区中的第N个位置获取消息
kafka-console-consumer.sh --bootstrap-server kafka-standalone:9092 --partition 0 --offset 4 --topic test
# 作为Consumer - 分组消息, 每组中只有一个消费者能收到消息
kafka-console-consumer.sh --bootstrap-server kafka-standalone:9092 --topic test --consumer-property group.id=testGroup

# 查看消费者组的情况
kafka-consumer-groups.sh --bootstrap-server kafka-standalone:9092 --describe --group testGroup
```

+ 每个单独消费者和每个消费组都会收到消息
+ 分组消费, 组中只有一个消费者会收到消息
+ 每个消费者组有自己的消费进度, 多个partition,每个partition记录多个记录

> 不同消费组处理不同业务 , 每个消费组用于负载均衡

## 核心概念

+ Broker: Kafka节点, 多个Broker组成一个集群, 每个Broker可以有多个Topic
+ Producer: 生产message发送到topic
+ Consumer: 订阅topic消费message, consumer作为一个线程来消费
+ Consumer Group: 一个Consumer Group包含多个Consumer
+ Topic(逻辑概念): 一种类别, 每条消息都有一个类别, 不同消息分开存储
+ Partition: topic物理上的分组, 一个topic可以分为多个partition, 每个partition是一个有序的队列
+ Replicas: 每一个分区, 会有N个副本
+ Segment: partition无力上由多个segment组成, 每个Segment存着message消息

![image-20230816160252884](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230816160252884.png)


## 集群

![image-20230816160841214](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230816160841214.png)

### 分组消息



## 顺序消费

topic只有一个partition, Consumer Group 中只有一个 Consumer


## TODO 


JPS ?