# Kafka

> 设计上允许少量消息丢失(大数据场景下), 通常用在允许少量数据丢失的情况, 多个版本迭代, 目前基本认为不会丢消息
> 
> 金融场景下不要使用Kafka, 建议使用 RocketMq

> Kafka在企业级应用中被广泛应用，包括实时流处理、日志聚合、监控和数据分析等方面。同时，Kafka还可以与其他大数据工具集成，如Hadoop、Spark和Storm等，构建一个完整的数据处理生态系统。

高可用性, 可能会有丢失数据的可能性, 功能比较单一, 适合日志分析, 大数据采集

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

## 单机启动

**启动zookeeper**

```shell
mkdir -p $KAFKA_HOME/data
vim $KAFKA_HOME/conf/zookeeper.properties

# 修改数据目录位置
# dataDir=/opt/software/kafka_2.12-3.6.1/data/zk
cd $KAFKA_HOME/bin

sh zookeeper-server-start.sh ../config/zookeeper.properties
```

**启动kafka**

```shell
mkdir -p $KAFKA_HOME/data
vim $KAFKA_HOME/conf/server.properties

# 修改数据目录位置
# log.dirs=/opt/software/kafka_2.12-3.6.1/data/kafka

cd $KAFKA_HOME/bin
sh kafka-server-start.sh ../config/server.properties
```

**JPS检查**

```shell
jps
# 2617501 QuorumPeerMain # Zookeeper
# 2621290 Jps
# 2619753 Kafka
```

**关闭:** 先关Kafka再关Zookeeper

## 简单收发消息

![image-20230816153616472](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230816153616472.png)

```shell
# 查看所有topic
./bin/kafka-topics.sh --list  --bootstrap-server <hostname>:9092
# 创建topic
# --topic：要创建的主题的名称。
# --partitions：主题的分区数。
# --replication-factor：每个分区的副本数。
./bin/kafka-topics.sh --create --topic <topic-name> --bootstrap-server <hostname>:9092
# 删除topic
./bin/kafka-topics.sh --delete --topic <topic-name> --bootstrap-server <hostname>:9092
# 查看topic
./bin/kafka-topics.sh --describe --topic <topic-name> --bootstrap-server <hostname>:9092 
```

```shell
# 作为Producer
./bin/kafka-console-producer.sh --bootstrap-server <hostname>:9092 --topic test
```

```shell
# 作为Consumer - 只能接收监听后生产的消息 - 每个消费者都会收到消息
./bin/kafka-console-consumer.sh --bootstrap-server <hostname>:9092 --topic test
# 作为Consumer - 从头接收消息
./bin/kafka-console-consumer.sh --bootstrap-server <hostname>:9092 --from-beginning --topic test
# 作为Consumer - 从0分区中的第N个位置获取消息
./bin/kafka-console-consumer.sh --bootstrap-server <hostname>:9092 --partition 0 --offset 4 --topic test
# 作为Consumer - 分组消息, 每组中只有一个消费者能收到消息
./bin/kafka-console-consumer.sh --bootstrap-server <hostname>:9092 --topic test --consumer-property group.id=testGroup

# 查看消费者组的情况
./bin/kafka-consumer-groups.sh --bootstrap-server <hostname>:9092 --describe --group testGroup
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
+ Replicas: 每一个分区, 会有N个副本, <= broker number - 1
+ Segment: partition无力上由多个segment组成, 每个Segment存着message消息

![image-20230816160252884](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230816160252884.png)

## 集群

![image-20230816160841214](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230816160841214.png)

![image-20230817103641095](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230817103641095.png)

1. Topic是一个逻辑概念，Producer和Consumer通过Topic进行业务沟通。 
2. Topic并不存储数据，Topic下的数据分为多组Partition，尽量平均的分散到各个Broker上
3. 每组 Partition可以不再一个Broker中, 包含Topic下一部分的消息。每组Partition包含一个Leader Partition以及若干个Follower Partition 进行备份，每组Partition的个数称为备份因子 replica factor
4. Producer将消息发送到对应的Partition上，然后Consumer通过Partition上的Offset偏移量，记录自 己所属消费者组Group在当前Partition上消费消息的进度
5. Producer发送给一个Topic的消息，会由Kafka推送给所有订阅了这个Topic的消费者组进行处理。但是 在每个消费者组内部，只会有一个消费者实例处理这一条消息。
6. Kafka的Broker通过Zookeeper组成集群。然后在这些Broker中，需要选举产生一个担任 Controller角色的Broker

## 服务端设计



## 官方客户端使用

> Kafka提供了两套客户端API，HighLevel API和LowLevel API。 HighLevel API封装了kafka的运行细节，使用起来比较简单，是企业开发过程中最常用的客户端API。

**官方包引入:**

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka_2.13</artifactId>
    <version>3.4.0</version>
</dependency>
```

### 生产者基本代码

```java
public class MyProducer {
    private static final String BOOTSTRAP_SERVERS = System.getenv().get("ENV_CLOUD_IP") + ":9092";
    private static final String TOPIC_NAME = "demoTopic";

    private static String messageKey = "message_key";

    private static String messageValue = "message_value";

    private static String sendType = "async"; // direct sync async

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // PART1:设置发送者相关属性
        Properties props = new Properties();
        // 此处配置的是kafka的端口
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // 配置key的序列化类
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        // 配置value的序列化类
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        // Part2:构建消息
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, messageKey, messageValue);
        // Part3:发送消息
        if (sendType.equals("direct")) { // 单向发送：不关心服务端的应答。
            producer.send(record);
            producer.close();
        } else if (sendType.equals("sync")) { // 同步发送：获取服务端应答消息前，会阻塞当前线程。
            RecordMetadata recordMetadata = producer.send(record).get();
            String topic = recordMetadata.topic();
            int partition = recordMetadata.partition();
            long offset = recordMetadata.offset();
            String message = recordMetadata.toString();
            System.out.println("message:[" + message + "] send with topic:" + topic + ";partition:" + partition + ";offset:" + offset);
            producer.close();
        } else if (sendType.equals("async")) { // // 异步发送：消息发送后不阻塞，服务端有应答后会触发回调函数
            CountDownLatch latch = new CountDownLatch(1);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (null != e) {
                        System.out.println("消息发送失败," + e.getMessage());
                        e.printStackTrace();
                    } else {
                        String topic = recordMetadata.topic();
                        long offset = recordMetadata.offset();
                        String message = recordMetadata.toString();
                        System.out.println("message:[" + message + "] send with opic:" + topic + ";offset:" + offset);
                    }
                    latch.countDown();
                }
            });
            latch.await();
            producer.close();
        }
    }
}
```

消息是一个Key-Value结构的消息。其中，key和value都可以是任意对象类型。其中，key主要是用来进行Partition分区的，业务上更关心的是value。

发送消息有3种方式:

+ 单向发送：不关心服务端的应答。
+ 同步发送：获取服务端应答消息前，会阻塞当前线程。
+ 异步发送：消息发送后不阻塞，服务端有应答后会触发回调函数。

### 消费者基本代码

```java
public class MyConsumer {
    private static final String BOOTSTRAP_SERVERS = System.getenv().get("ENV_CLOUD_IP") + ":9092";
    private static final String TOPIC_NAME = "demoTopic";
    private static final String CONSUMER_GROUP_NAME="demoGroup";

    public static void main(String[] args) throws InterruptedException {
        // PART1:设置发送者相关属性
        Properties props = new Properties();
        // kafka地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        // 每个消费者要指定一个group
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_NAME);
        // key序列化类
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        // value序列化类
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        while (true){
            // PART2:拉取消息
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofNanos(100)); // 100毫秒超时时间
            // PART3:处理消息
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("offset = " + record.offset() + ";key = " + record.key() + "; value= " + record.value());
            }
            consumer.commitSync(); //同步提交，表示必须等到offset提交完毕，再去消费下一批数据。
            // consumer.commitAsync(); //异步提交，表示发送完提交offset请求后，就开始消费下一批数据了。不用等到Broker的确认
            Thread.sleep(50);
        }
    }
}
```

Kafka采用Consumer主动拉取消息的Pull模式。

消费消息后, 需要像Broker提交偏移量Offset, 若不提交认为消费失败了, 还会进行重复推送

## 消费者分组消费机制

![image-20230818091801388](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230818091801388.png)

+ 生产者往Topic下发消息时，会尽量均匀的将消息发送到Topic下的各个Partition当中
+ 这个消息，会向 所有订阅了该Topic的消费者推送
+ 一个消费者组中只会有一个消费者消费这个消息
+ 不同消费者组之间，会重复消费消息副本

> 一个Partition只能有一个组中的Consumer
>
> 一个组中的Consumer可以对应多个Partition

每个消费者组有一个Offset偏移量, 量表示每个消费者组在每个Partiton中已经消费处理的进度。

```shell
kafka-consumer-groups.sh --bootstrap-server <broder-ip>:<broder-port> --describe --group <group-name>
```

**自动提交offset配置:**

```java
// ConsumerConfig
public static final String ENABLE_AUTO_COMMIT_CONFIG = "enable.auto.commit"; // true
```

由于某些原因, 可能存在服务端Offset不存在的情况, 可配置如下兜底方案:

```java
public static final String ENABLE_AUTO_COMMIT_CONFIG = "enable.auto.commit"; // true
```

```java
// ConsumerConfig
/**
* earliest ： 自动设置为当前最早的offset
* latest ：自动设置为当前最晚的offset
* none ： 向Consumer抛异常
*/
public static final String AUTO_OFFSET_RESET_CONFIG = "auto.offset.reset";
```

TODO: 合理配置partition

## 生产者拦截器

生产者拦截机制允许客户端在生产者在消息发送到Kafka集群之前，对消息进行拦截，甚至可以修改消息内容。

```java
// ConsumerConfig
public static final String INTERCEPTOR_CLASSES_CONFIG = "interceptor.classes";
```

```java
public class MyInterceptor implements ProducerInterceptor {
    // 发消息时触发
    @Override
    public ProducerRecord onSend(ProducerRecord producerRecord) {
        return producerRecord;
    }
    // 收到服务端响应时触发
    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }
    // 连接关闭时触发
    @Override
    public void close() {

    }
    // 整理配置项
    @Override
    public void configure(Map<String, ?> map) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            System.out.println("entry.key:" + entry.getKey() + " === entry.value:" + entry.getValue());
        }
    }
}
```

## 生产者消息缓存机制

生产者发送消息时并不是一条一条发往服务器, 而是增加了一个高速缓存, 进行批量发送

的主要目的是将消息打包，减少网络IO频率

![image-20230818105336707](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230818105336707.png)

![image-20230818105347598](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230818105347598.png)

默认LINGER_MS_CONFIG配置时0, 即未使用缓存

可对此进行配置, 当无负载时, 最多增加X毫秒的延时

```java
// ProducerConfig
public static final String LINGER_MS_CONFIG = "linger.ms";
```

## Producer消息分区路由

默认Producer发送消息时:

+ 传入KEY :  会根据KEY的HASH指定一个Partition
+ 未传入KEY : 尽量像一个分区内发, 直到达到BATCH_SIZE_CONFIG, 在像下一个分区内发送

通过修改`PARTITIONER_CLASS_CONFIG`修改默认发送消息路由

```java
// ProducerConfig
public static final String PARTITIONER_CLASS_CONFIG = "partitioner.class";
```

+ 轮询机制:  org.apache.kafka.clients.producer.RoundRobinPartitioner
  + 基本不用
+ 自定义路由: 实现org.apache.kafka.clients.producer.Partitioner接口, 创建自己的路由逻辑

```java
public class MyPartitioner implements Partitioner {
    // 指定哪个Partition
    @Override
    public int partition(String topicName, Object messageKey, byte[] bytes, Object messageValue, byte[] bytes1, Cluster cluster) {
        int i = messageKey.hashCode();
        // 当前topic的所有Partition
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topicName);
        int numPartitions = partitions.size();
        return Math.abs(i % numPartitions);
    }

    @Override
    public void close() {

    }
    // 整理配置项
    @Override
    public void configure(Map<String, ?> map) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            System.out.println("entry.key:" + entry.getKey() + " === entry.value:" + entry.getValue());
        }
    }
}
```

## 发送应答机制

```java
// ProducerConfig
public static final String ACKS_CONFIG = "acks";
```

该配置用于确保消息的安全性

+ acks=0 : 生产者不关心Broker端有没有将消息写入到Partition
+ acks=1 : Leader Partition在完成自己的消息写入后，就向生产者返回 结果
  + 一般用于传输日志等, 允许个别数据丢失的场景
+ acks=all : 生产者需要等Broker端的所有Partiton(Leader Partition以及Follower Partition都写完才返回
  + 一般用于传输敏感数据, 比如钱相关的数据

在Kafka服务端有一个配置参数`min.insync.replicas`控制多个Partition节点写入消息后, 像Producer返回响应

若希望配置为超过半数节点写入成功就发送应答, 可配置`acks=all`, `min.insync.replicas`为`partitions / 2 + 1`, 但该配置时全局的, 须保证适应所有topic

## 生产者消息幂等

若网络出现问题, 虽然消息已经存盘, 但ack应答并未返回导致异常, 那么Producer会发起重试, 重试次数由`RETRIES_CONFIG`控制, 默认为`Integer.MAX`

```java
// ProducerConfig
public static final String RETRIES_CONFIG = "retries";
```

此时, 会出现消息重复现象, 需要保证当发送重复消息时, Broker端只保留一条消息, 使用`ENABLE_IDEMPOTENCE_CONFIG`进行配置

```java
// ProducerConfig
public static final String ENABLE_IDEMPOTENCE_CONFIG = "enable.idempotence"; // true
```

该配置默认启用, 但须保证 acks=all

**幂等性实现方式:**

> 类似乐观锁机制

+ PID:  每个Producer初始化时被赋予一个PID
+ Sequence Numer:  每个Producer针对Partition会维护一个Sequence Numer(从0开始单调递增的数字), 发消息时Sequence Numer加1并随之发送
+ SN: Broker端针对每个PID, 维护一个序列号(SN), 收到消息时和Sequence Numer进行比对

当 SN + 1 = Sequence Numer 时, 接收消息并 SN = SN + 1

当 SN + 1 > Sequence Numer 时, 认为消息已写入, 仅发送ack

当 SN + 1 < Sequence Numer 时, 认为有消息丢失, 向Producer抛出`OutOfOrderSequenceException`

![image-20230818125338656](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230818125338656.png)

## 生产者消息事务 

过生产者消息幂等性问题，能够解决单生产者消息写入单分区的的幂等性问题

但当批量发送消息, 消息指向不同Partition, 此时仅通过幂等性配置无法保证所有消息的幂等性

> 使用事务机制, 保证一批消息要么同时成功, 保持幂等性, 要么同时失败, 可进行整体重试

```java
// 1 初始化事务
void initTransactions();
// 2 开启事务
void beginTransaction() throws ProducerFencedException;
// 3 提交事务
void commitTransaction() throws ProducerFencedException;
// 4 放弃事务（类似于回滚事务的操作）
void abortTransaction() throws ProducerFencedException;
```

## SpringBoot集成Kafka

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```



## 消费者消息幂等





## 数据清理机制

## 顺序消费

1. 需要保证顺序的消息发送到相同的Partition中
2. 客户端单线程消费对应的Partition

## 优化

1. 顺序消费下, 因网络不稳定导致某个消息消费失败, 后续消息消费依赖前一条消息的业务执行, 导致所有消息失败

增加重试机制, 超过重试次数后, 将消息加入到重试表中, 之后消息判断重试表中是否有业务ID相关的重新信息, 无则继续执行, 有则加入重试表

TODO elastic-job建立失败重试机制


## TODO 

消息积压- 如何发生的

分区如何设计

消费者多线程消费

解析pojo

多个生产者分别指定路由机制

多线程情况下, offset更新

如何压测



干货:

https://www.bilibili.com/video/BV1gm4y1w7tN/?spm_id_from=333.337.search-card.all.click&vd_source=0494972cf815a7a4a8ac831a4c0a1229