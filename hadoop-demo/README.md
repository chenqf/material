# Hadoop

## 大数据

+ 数据接入部分
+ 数据计算部分 - 最重要
    + MapReduce
    + Spark - 主流
    + Flink - 主流
+ 结果存储部分

## 运行模式

+ 本地运行
    + 数据存储在本地
    + 基本不用, 只用作学习
+ 伪分布式
    + 数据存储在HDFS
+ 完全分布式
    + 存储在HDFS/多台服务器工作
    + 企业中大量使用

## 集群

#### 规划

|      | server1                | server2                          | server3                         |
|------|------------------------|----------------------------------|---------------------------------|
| HDFS | NameNode<br />DataNode | DataNode                         | SecondaryNameNode<br />DataNode |
| YARN | NodeManager            | NodeManager<br />ResourceManager | NodeManager                     |

+ NameNode和SecondaryNameNode 不要安装在同一台服务器
+ ResourceManager 也很消耗内存，不要和NameNode、SecondaryNameNode配置在 同一台机器上。

#### 配置文件

+ 默认配置
    + core-default.xml
    + hdfs-default.xml
    + yarn-default.xml
    + mapred-default.xml
+ 自定义配置 - 实际用来配置的地方
    + core-site.xml
    + hdfs-site.xml
    + yarn-site.xml
    + mapred-site.xml

> $HADOOP_HOME/etc/hadoop/core-site.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
    <!-- 指定NameNode的地址 -->
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://server1:8020</value>
    </property>

    <!-- 指定hadoop数据的存储目录 -->
    <property>
        <name>hadoop.tmp.dir</name>
        <value>/opt/module/hadoop-3.1.3/data</value>
    </property>

    <!-- 配置HDFS网页登录使用的静态用户为chenqf -->
    <property>
        <name>hadoop.http.staticuser.user</name>
        <value>chenqf</value>
    </property>
</configuration>
```

> $HADOOP_HOME/etc/hadoop/hdfs-site.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
    <!-- nn web端访问地址-->
    <property>
        <name>dfs.namenode.http-address</name>
        <value>server1:9870</value>
    </property>
    <!-- 2nn web端访问地址-->
    <property>
        <name>dfs.namenode.secondary.http-address</name>
        <value>server3:9868</value>
    </property>
</configuration> 
```

> $HADOOP_HOME/etc/hadoop/yarn-site.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <!-- 指定MR走shuffle -->
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>

    <!-- 指定ResourceManager的地址-->
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>server2</value>
    </property>

    <!-- 环境变量的继承 -->
    <property>
        <name>yarn.nodemanager.env-whitelist</name>

        <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CO
            NF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAP
            RED_HOME
        </value>
    </property>
</configuration> 
```

> $HADOOP_HOME/etc/hadoop/mapred-site.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
    <!-- 指定MapReduce程序运行在Yarn上 -->
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    <!-- 历史服务器端地址 -->
    <property>
        <name>mapreduce.jobhistory.address</name>
        <value>server1:10020</value>
    </property>
    <!-- 历史服务器web端地址 -->
    <property>
        <name>mapreduce.jobhistory.webapp.address</name>
        <value>server1:19888</value>
    </property>
</configuration> 
```

> $HADOOP_HOME/etc/hadoop/workers

```shell
server1
server2
server3
```

#### 启动

#### 启动HDFS

> 第一次启动, 需要在 `NameNode(server1)` 节点格式化 NameNode
>
> 格式化 NameNode , 会产生新的集群ID, 导致 NameNode 和 DataNode 的集群ID不一致, 导致找不到以往的数据
>
> 如果一定要重新格式化, 需删除所有节点的 data 和 logs 目录

```shell
hdfs namenode -format
```

> 在server1节点进行启动, 相互间必须设置免密登录, 自动将其他节点启动

```shell
cd ${HADOOP_HOME}
sbin/start-dfs.sh
# 启动后查看 jps
```

> web界面查看HDFS的NameNode, 以及HDFS上存储的数据信息

```shell
http://server1:9870/dfshealth.html
```

#### 启动Yarn

> 在`ResourceNode(server2)`节点启动

```shell
cd ${HADOOP_HOME}
sbin/start-yarn.sh
# 启动后查看 jps
```

> web端查看YARN的ResourceManager, 查看YARN上运行的Job信息

```shell
http://server2:8088
```

#### 启动历史服务器

在server1启动历史服务器

```shell
mapred --daemon start historyserver
```

查看:

```shell
http://server1:19888
```

#### 日志聚集

<img src="https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240429103211903.png" alt="image-20240429103211903"  />

> ：开启日志聚集功能，需要重新启动 NodeManager 、ResourceManager 和 HistoryServer

配置yarn-site.xml追加 :

```xml
<!-- 开启日志聚集功能 -->
<property>
    <name>yarn.log-aggregation-enable</name>
    <value>true</value>
</property>
        <!-- 设置日志聚集服务器地址 -->
<property>
<name>yarn.log.server.url</name>
<value>http://server1:19888/jobhistory/logs</value>
</property>
        <!-- 设置日志保留时间为7天 -->
<property>
<name>yarn.log-aggregation.retain-seconds</name>
<value>604800</value>
</property> 
```

关闭NodeManager 、ResourceManager和HistoryServer

```shell
sbin/stop-yarn.sh
mapred --daemon stop historyserver
```

启动NodeManager 、ResourceManage和HistoryServer

```shell
sbin/start-yarn.sh 
mapred --daemon start historyserver
```

## HDFS

![image-20240506105158771](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240506105158771.png)

#### NameNode - 管理者-Master

+ 管理HDFS的名称空间
+ 配置副本策略
+ 管理数据块(Block)映射信息
+ 处理客户端读写请求

> 文件块大小默认128M, 一般128M或256M (配置: dfs.blocksize) (代表上线)
>
> 若1kb的文件进行存储, 会找到一个block并暂用1kb的空间, bock的其他空间其他文件也可以使用
>
> 问文件超过block的上限, 就会对文件进行切分

1. 若寻址时间为10ms, 则查找到目标block的时间10ms
2. 寻址时间为传输时间的1%时, 为最佳状态, 因此 传输时间 = 10ms/0.01 = 1s
3. 目前普通磁盘的传输速率为100MS/s, 所以128最接近100, 所以默认为128M

> block设置过大, 会增加寻址时间
>
> block设置过小, 磁盘传输数据时间会明显大于定位时间, 导致处理块数据非常慢

结论: block大小取决于磁盘传输速率

#### DataNode - 实际操作 - Slave

+ 集群中实际存储数据的节点
+ 定期向NameNode报告它们所存储的块信息，并根据NameNode的指示进行数据块的复制和移动

#### Secondary NameNode - 非热备

+ 辅助NameNode,分担工作量
+ 紧急情况下, 可辅助恢复NameNode

### Client - 客户端

+ 文件切分, 将文件切分为一个一个的Block, 然后上传
+ 与NameNode交互, 获取文件的位置信息
+ 与DataNode交互, 读取或写入数据
+ 提供命令访问管理HDFS

#### 基本命令

```shell
# 查询命令用法
hadoop fs -help <command>
# 本地剪切
hadoop fs -moveFromLocal <local-file-path> <hdfs-file-dir>
# 本地复制
hadoop fs -copyFromLocal <local-file-path> <hdfs-file-dir>
hadoop fs -put <local-file-path> <hdfs-file-dir>
# 追加文件内容到文件末尾
hadoop fs -appendToFile <local-file-path> <hdfs-file-path>
# 下载
hadoop fs -copyToLocal  <hdfs-file-path> <local-file-dir>
hadoop fs -get  <hdfs-file-path> <local-file-dir>
# 其他
hadoop fs -ls <hdfs-file-dir>
hadoop fs -cat <hdfs-file-path>
hadoop fs -mkdir <hdfs-file-dir>
hadoop fs -cp <hdfs-file-path> <hdfs-cp-file-dir>
hadoop fs -mv <hdfs-file-path> <hdfs-mv-file-dir>
hadoop fs -rm <hdfs-file-path>
hadoop fs -tail <hdfs-file-path> # 显示一个文件末尾的1kb的数据
hadoop fs -du -s -h <hdfs-file-dir> # 统计文件夹总大小信息
hadoop fs -du -h <hdfs-file-dir> # 统计文件夹下文件大小信息
```

#### Java环境API

> windows下需本地存在hadoop并配置环境变量

```xml

<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-client</artifactId>
    <version>3.1.3</version>
</dependency>
```

```java
public class Demo {

    private FileSystem fs;

    @Before
    public void init() throws URISyntaxException, IOException, InterruptedException {
        Configuration configuration = new Configuration();

        // 指定nameNode所在节点, 指定用户名
        fs = FileSystem.get(new URI("hdfs://192.168.10.101:8020"),
                configuration, "chenqf");
    }

    @After
    public void close() throws IOException {
        fs.close();
    }

    @Test
    public void testMkdir() throws IOException {
        fs.mkdirs(new Path("/xiyou1/"));
    }

    @Test
    public void testPut() throws IOException {
        fs.copyFromLocalFile(false, true, new Path("D:\\hdfs-demo.txt"), new Path("hdfs://server1/xiyou/huaguoshan/"));
    }

}
```

#### 参数优先级

1. 代码中配置
2. 项目/resource/hdfs-site.xml
3. node-config/hdfs-site.xml
4. node-config/hdfs-default.xml

### HDFS 写数据流程

![image-20240506160349884](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240506160349884.png)

### HDFS 读数据流程

![image-20240507083741782](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240507083741782.png)

### NameNode 工作流程

![image-20240507085729325](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240507085729325.png)

阶段一: NameNode启动

+ 启动NameNode格式化
    + 第一次启动, 创建FsImage和Edits文件
    + 非第一次启动, 直接加载FsImage和Edits到内存
+ 客户端对元数据修改
    + 记录操作日志(滚动追加Edits)
    + 在内存中进行修改

阶段二: Secondary NameNode工作

+ Secondary NameNode 询问 NameNode 是否需要 CheckPoint。直接带回 NameNode 是否检查结果
+ Secondary NameNode 请求执行 CheckPoint
+ NameNode滚动正在写的Edits日志
+ 将滚动前的编辑日志和镜像文件拷贝到Secondary NameNode。
+ Secondary NameNode 加载编辑日志和镜像文件到内存，并合并。
+ 生成新的镜像文件fsimage.chkpoint。
+ 拷贝fsimage.chkpoint 到 NameNode。
+ NameNode将fsimage.chkpoint 重新命名成fsimage。

### DataNode 工作机制

![image-20240507090338170](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20240507090338170.png)

## Yarn

#### NodeManager

+ 负责单个节点上的资源管理和任务调度
+ 接收来自ResourceManager的指令，并管理节点上的资源，包括内存、CPU等
+ 负责启动和监控由ResourceManager分配给该节点的任务容器

#### ResourceManager

+ 负责整个集群的资源管理和作业调度
+ 接收作业的提交请求，并根据集群资源的可用性来为作业分配资源
+ 负责监控节点状态、维护资源队列等功能，以确保集群的高效利用和公平共享