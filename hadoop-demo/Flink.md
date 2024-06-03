# Flink

> 目标: 数据流上的有状态计算

## 概念

#### 无界流

+ 有定义流的开始, 但没有定义流的结束
+ 会无休止的产生数据
+ 数据必须持续处理, 被摄取后需要立即处理

> 比如获取Kafka中的数据

#### 有界流

+ 有定义流的开始, 也定义流的结束
+ 可以在摄取所有数据后再进行计算
+ 数据可被排序

> 比如获取文件中的内容

#### 特点

+ 高吞吐低延迟, 每秒百万个事件, 毫秒级延迟
+ 结果的准确性, 提供了 事件时间(event-time) 和 处理时间(processing-time)
+ 精确一次(exactly-once)的状态一致性保证 (不丢数, 不重复)

#### 分层API

1. 有状态流处理 - 底层APIs(处理函数)
2. DataStream API - 核心 APIs
3. Table API - 声明式领域专用语言
4. SQL - 最高层语言 - 用的最多

## 集群

<img src="https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240425143628667.png" alt="image-20240425143628667" style="zoom:50%;" />

+ 客户端(Client) : 代码由客户端获取并做转换, 之后提交给JobManger
+ JobManger : 集群中的"管事人", 对作业进行中央调度管理; 获取到要执行的作业后, 进一步转换处理, 饭后分发任务给TaskManger
+ TaskManger: 工作节点, 真正干活的人

### 集群配置

集群规划

| 节点服务器 | server1                     | server2     | server3     |
|-------|-----------------------------|-------------|-------------|
| 角色    | JobManager<br />TaskManager | TaskManager | TaskManager |

```shell
# 每个节点都改
vim $FLINK_HOME/conf/flink-conf.yaml
```

```yaml
jobmanager.rpc.address: server1
jobmanager.bind-host: 0.0.0.0
rest.address: server1
rest.bind-address: 0.0.0.0
# 改成自己当前节点
taskmanager.bind-host: server1 # server1/server2/server3
taskmanager.host: 0.0.0.0
```

```shell
vim $FLINK_HOME/conf/workers

# 修改为:
server1
server2
server3
```

```shell
vim $FLINK_HOME/conf/masters

# 修改为:
server1:8081
```

**启动集群(Standalone - 会话模式)**

```shell
# server1
$FLINK_HOME/bin/start-cluster.sh 
```

**停止集群(Standalone - 会话模式)**

```shell
# server1
$FLINK_HOME/bin/stop-cluster.sh 
```

**命令执行java任务**

```shell
$FLINK_HOME/bin/flink run -m server1:8081 -c com.maple.flink.FlinkDemo /opt/myjar/hadoop-demo-0.0.1-SNAPSHOT.jar
```

## 部署模式

+ 会话模式 Session Mode
+ 单作业模式 Per-Job Mode
+ 应用模式 Application Mode

### 会话模式

先启动一个集群, 保持一个会话, 客户端提交的作业竞争集群中的资源

<img src="https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240426111930380.png" alt="image-20240426111930380" style="zoom:50%;" />

适用于`单个规模小, 执行时间短的大量作业`, 多用于开发测试阶段

> main 方法在客户端执行

### 单作业模式

为了更好的隔离资源, `每个提交的作业启动一个集群`

<img src="https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240426112050988.png" alt="image-20240426112050988" style="zoom:50%;" />

作业完成后, 集群就会关闭, 所有资源都会被释放

> 生产环境更加稳定, 1.17版本前, 实际应用的`首选模式`,但需要更多的资源
>
> main 方法在客户端执行

### 应用模式

> 前两种模式, 应用代码都是在客户端上执行, 由客户端提交给 JobManager

> 前两种模式, 客户端会占用大量网络带宽, 很多情况下提交作业用的是同一个客户端, 会加重客户端所在节点的资源消耗

> main 方法在JobManager中执行

应用模式:

+ 每个提交的应用, 单独启用一个JobManager(规避客户端), 也就是创建一个集群
+ 这个JobManager只为执行这一个应用而存在, 执行结束JobManager也就关闭了

<img src="https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240426112901014.png" alt="image-20240426112901014" style="zoom:50%;" />

> 独立集群, 解决了Pre-Job Mode的痛点, 目前官方主推

## 运行模式

+ Standalone
+ YARN
+ K8S

### Yarn 部署

环境变量

```shell
#FLINK_HOME
export HADOOP_CLASSPATH=`hadoop classpath`
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
```

#### Yarn 部署会话模式

**开启Flink**

```shell
$FLINK_HOME/bin/yarn-session.sh -d  -nm test-flink # -d 后台启动 -nm test-flink 指定yarn中的名字为test-flink
```

**命令行提交**

```shell
$FLINK_HOME/bin/flink run -c com.maple.flink.FlinkDemo /opt/myjar/hadoop-demo-0.0.1-SNAPSHOT.jar
```

**关闭Flink**

```shell
# 此命令来源于开启Flink时的日志
echo "stop" | $FLINK_HOME/bin/yarn-session.sh -id application_1714358324726_0003
```

#### Yarn 部署单作业模式

**启动**

```shell
$FLINK_HOME/bin/flink run -d -t yarn-per-job -c com.maple.flink.FlinkDemo $FLINK_HOME/lib/hadoop-demo-0.0.1-SNAPSHOT.jar
```

#### Yarn 部署应用模式

## Flink 运行时架构

### 并行子任务和并行度

每个算子(operator)包含一个或多个子任务(operator subtask), 这些子任务在不同的线程不同的物理机或不同的容器中完全独立的执行

> 特定算子的子任务个数, 被称为`并行度`(parallelism), 默认为电脑的线程数

+ 代码层面全局设定

```java
public class FlinkDemo {
    public static void demo() {
        //        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // IDEA 运行时, 也可以看到webui, 一般用于本地测试 localhost:8081
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        env.setParallelism(3);
    }
}
```

### 算子链 (Operator Chain)

数据流在算子之间的传输形式:

+ 一对一, 直通模式 (one-to-one / forwarding)
+ 重分区 (Redistributing)

#### 一对一

算子间`不需要重新分区`, 也`不需要调整数据的顺序` (map / filter / flatMap 等)

#### 重分区

算子间`根据数据传输的策略, 把数据发送到不同的下游目标任务`

除了`forwarding`之外, 都是重分区

#### 合并算子链

并行度相同的`一对一`算子操作, 可以直接链接在一起形成一个`大`的任务(Task)

### 任务槽 (Task Slot)

TaskManager拥有计算资源的一个固定大小的子集, 这些资源是用来执行一个子任务的

TaskManager管理的内存平分,每个Slot会将独占一份, 仅仅是内存的隔离, 没有CPU的隔离

不同算子的并行子任务, 可以放到同一个Slot中执行

![image-20240522150406412](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240522150406412.png)

#### Slot 设置

```shell
vim $FLINK_HOME/conf/flink-conf.yaml

# 每个taskManager有几个Slot
# 建议设置为配置机器的CPU核心数
# taskmanager.numberOfTaskSlots: 1 
```

#### Slot 和 并行度的关系

Slot 是静态的概念,是指TaskManager的并发能力, 是并行的上限

并行度是动态的概念, 是TaskManager运行时的`实际并发能力`

> Slot的数量 >= job并行度(算子最大并行度), job才能运行

## DataStream API

### 执行环境 Execution Environment

```java
public class EnvDemo {
    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration(); // 用于修改一些参数

        conf.set(RestOptions.BIND_PORT, "8081");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);

        // 流批一体, 默认为流方式
        // 此种方式不常用, 一般通过命令行配置来进行修改 -Dexecution.runtime-mode=BATCH
        // env.setRuntimeMode(RuntimeExecutionMode.BATCH); // 批处理方式来执行

        //....

        // 懒执行 / 延迟执行
        env.execute();  // 必须写
    }
}
```

### 源算子 Source Operator

数据的输入来源称为数据源(datasource), 读取数据的算子就是源算子(source operator)

**准备:**

```xml

<dependencies>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
        <version>1.17.2</version>
    </dependency>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-clients</artifactId>
        <version>1.17.2</version>
    </dependency>
</dependencies>
```

#### 从集合中读取

```java
public class FromCollection {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 从集合中接收数据
        // DataStreamSource<Integer> source = env.fromCollection(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0));

        // 直接写数据
        DataStreamSource<Integer> source = env.fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);

        source.print();

        env.execute();
    }
}
```

#### 从文件中读取

```xml

<dependencies>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-connector-files</artifactId>
        <version>1.17.2</version>
    </dependency>
</dependencies>
```

```java
public class FromFile {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 从文件中读
        FileSource<String> source = FileSource.forRecordStreamFormat(new TextLineInputFormat(), new Path("C:\\work\\java-workspace\\material\\hadoop-demo\\src\\main\\java\\com\\maple\\flink\\source\\word.txt")).build();
        DataStreamSource<String> fromFile = env.fromSource(source, WatermarkStrategy.noWatermarks(), "fromFile");
        fromFile.print();

        env.execute();
    }
}
```

#### 从Kafka中读取

```xml

<dependencies>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-connector-kafka</artifactId>
        <version>1.17.2</version>
    </dependency>
</dependencies>
```

```java
public class FromKafka {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        // 从Kafka中读
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("62.234.18.108:9092")  // 必填：指定broker连接信息 (为保证高可用,建议多指定几个节点)
                .setGroupId("chenqf-kafka-group")  // 必填：指定消费者的groupid(不存在时会自动创建)
                .setTopics("demoTopic1") // 必填：指定要消费的topic
                .setValueOnlyDeserializer(new SimpleStringSchema()) // 必填：指定反序列化器(用来解析kafka消息数据)
                .setStartingOffsets(OffsetsInitializer.latest())// 起始位置 默认:earliest 一定从最早开始 latest 一定从最新消费 (与Kafka不一致)
                .build();

        DataStreamSource<String> fromKafka = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "fromKafka");
        fromKafka.print();

        env.execute();
    }
}
```

#### 数据生成器 - 测试 - 压测

```xml

<dependencies>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-connector-datagen</artifactId>
        <version>1.17.2</version>
    </dependency>
</dependencies>
```

```java
public class FromDataGen {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 如果有多个并行度, 将数值均分成n份
        env.setParallelism(2);
        /**
         * 数据生成器
         * 参数:
         * 1. GeneratorFunction接口, 重写map方法, 输入类型固定是Long
         * 2. Long类型, 自动生成的数字序列, 从1自增
         * 3. 限速策略, eg: 每秒几条
         * 4. 返回的类型
         */
        DataGeneratorSource<String> source = new DataGeneratorSource<>(
                // GeneratorFunction接口, 重写map方法, 输入类型固定是Long
                new GeneratorFunction<Long, String>() {
                    private static final long serialVersionUID = 7096925423962680215L;

                    @Override
                    public String map(Long value) throws Exception {
                        return "Number" + value;
                    }
                },
                10, // Long类型, 自动生成的数字序列, 从0自增 Long.MAX_VALUE : 实现无界流
                RateLimiterStrategy.perSecond(1), // 限速策略, eg: 每秒几条
                Types.STRING // 返回的类型
        );

        env.fromSource(source, WatermarkStrategy.noWatermarks(), "data-generator").print();

        env.execute();

        env.close();
    }
}
```

### 转换算子

```java

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaterSensor {
    /**
     * 水位传感器类型
     */
    private String id;
    /**
     * 水位传感器记录时间戳
     */
    private Long ts;
    /**
     * 水位记录
     */
    private Integer vc;
}
```

#### map 一一映射

```java
public class MapDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("s1", 1L, 1),
                new WaterSensor("s2", 2L, 2),
                new WaterSensor("s3", 3L, 3)
        );

        // 方式一: 匿名内部类 将WaterSensor转换为String:WaterSensor.getId()
//        SingleOutputStreamOperator<String> map = sensorDS.map(new MapFunction<WaterSensor, String>() {
//            private static final long serialVersionUID = -8875810405517918745L;
//
//            @Override
//            public String map(WaterSensor value) throws Exception {
//                return value.getId();
//            }
//        });

        // 方式二: lambda表达式 将WaterSensor转换为String:WaterSensor.getId()
        SingleOutputStreamOperator<String> map = sensorDS.map(WaterSensor::getId);

        map.print();

        env.execute();
    }
}
```

#### filter 过滤

```java
public class FilterDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("s1", 1L, 1),
                new WaterSensor("s2", 2L, 2),
                new WaterSensor("s3", 3L, 3)
        );

        SingleOutputStreamOperator<WaterSensor> filter = sensorDS.filter(value -> "s1".equals(value.getId()));

        filter.print();

        env.execute();
    }
}
```

#### flatMap 扁平映射 - 一进多出

```java
 // 如果输入是 s1 , 打印 vc, 如果是 s2 打印 vc ts
public class FlatMapDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("s1", 1L, 1),
                new WaterSensor("s1", 11L, 11),
                new WaterSensor("s2", 2L, 2),
                new WaterSensor("s2", 22L, 22)
        );

        // 方式一: 匿名内部类
        SingleOutputStreamOperator<String> flatMap = sensorDS.flatMap(new FlatMapFunction<WaterSensor, String>() {
            private static final long serialVersionUID = -8403006960028838517L;

            @Override
            public void flatMap(WaterSensor waterSensor, Collector<String> out) throws Exception {
                if ("s1".equals(waterSensor.getId())) {
                    out.collect(waterSensor.getVc().toString());
                } else if ("s2".equals(waterSensor.getId())) {
                    out.collect(waterSensor.getVc().toString());
                    out.collect(waterSensor.getTs().toString());
                }
            }
        });


        flatMap.print();

        env.execute();
    }
}

```

### 聚合算子 Aggregation

计算结果不仅依赖当前数据, 还和执勤啊数据有关, 相当于把所有数据聚在一起

#### keyBy 按键分区

要聚合, 需要先分区, 通过key将流从逻辑上划分为不同的分区(partition)

keyBy: 对数据分组, 保证相同key的数据, 在同一个分区
分区: 一个子任务, 可以理解为一个分区, `一个分区可以有多个分组`

```java
// 按照Id进行分组
public class KeyByDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("s1", 1L, 1),
                new WaterSensor("s2", 2L, 2),
                new WaterSensor("s3", 3L, 3),
                new WaterSensor("s4", 4L, 4)
        );

        // 按照Id进行分组
        // 不是转换算子, 只是重分区, 不能设置并行度
        KeyedStream<WaterSensor, String> keyBy = sensorDS.keyBy(new KeySelector<WaterSensor, String>() {
            private static final long serialVersionUID = -8029649922091486537L;

            @Override
            public String getKey(WaterSensor waterSensor) throws Exception {
                return waterSensor.getId();
            }
        });

        keyBy.print();

        env.execute();
    }
}
```

#### sum / min / max / minBy / maxBy

必须和 keyBy 成对出现

max/maxBy :

+ max只会取比较字段的最大值
+ maxBy不仅取比较字段的最大值,同时非比较字段取最大值这表数据的值

min/minBy : 同理

```java
public class SimpleDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("s1", 1L, 1),
                new WaterSensor("s1", 2L, 2),
                new WaterSensor("s2", 3L, 3),
                new WaterSensor("s3", 4L, 4),
                new WaterSensor("s3", 5L, 5)
        );
        KeyedStream<WaterSensor, String> keyBy = sensorDS.keyBy(new KeySelector<WaterSensor, String>() {
            private static final long serialVersionUID = -8029649922091486537L;

            @Override
            public String getKey(WaterSensor waterSensor) throws Exception {
                return waterSensor.getId();
            }
        });

        keyBy.sum("vc").print();

        env.execute();
    }
}
```