# Kafka

> 设计上允许少量消息丢失(大数据场景下), 通常用在允许少量数据丢失的情况, 多个版本迭代, 目前基本认为不会丢消息
> 
> 金融场景下不要使用Kafka, 建议使用 RocketMq

## JMS概念

P2P消息传输模型: 

+ 一个消息只有一个消费者, 一旦被消费, 就从队列中移除
+ 发送者和接受者在时间上没有依赖性
+ 接受者在成功收到消息后, 需要发送确认通知(ack)


Pub/Sub模型:

+ 每个消息可以有多个消费者
+ 发布者和订阅者之间有时间上的依赖性
+ 收到消息后队列不会删除该消息

接收消息方式:

+ 同步: 消费者调用receive()方法, receive()方法中, 消息未到达之前会阻塞
+ 异步: 消费者注册一个监听者, 只要消息到达, 监听者的onMessage()方法会被调用

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

## 核心概念

+ Broker: Kafka节点, 多个Broker组成一个集群, 每个Broker可以有多个Topic
+ Producer: 生产message发送到topic
+ Consumer: 订阅topic消费message, consumer作为一个线程来消费
+ Consumer Group: 一个Consumer Group包含多个Consumer, 预先在配置文件中配置好的
+ Topic(逻辑概念): 一种类别, 每条消息都有一个类别, 不同消息分开存储, 每个topic可以认为是一个队列
+ Partition: topic物理上的分组, 一个topic可以分为多个partition, 每个partition是一个有序的队列
+ Replicas: 每一个分区, 会有N个副本
+ Segment: partition无力上由多个segment组成, 每个Segment存着message消息



## 顺序消费

topic只有一个partition, Consumer Group 中只有一个 Consumer


## TODO 


JPS ?