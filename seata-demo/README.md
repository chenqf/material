# 分布式事务

`CAP理论(布鲁尔定理)`: 分布式事务中无法同时满足 Consistency / Availability / Partition tolerance

+ `C: 一致性`, 数据操作要么全部成功, 要么全部失败
+ `A: 可用性` , 各个节点必须可用, 客户端访问时, 不能出现等待/超时/报错
+ `P: 分区容错性`, 多个节点, 就算节点之间通信失败, 也能正常对外提供服务

> 解决分布式事务, 遵循CP或者AP

## 典型应用场景

**单体应用多数据源**

![image-20231007135722356](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007135722356.png)

**分库分表** (一般使用XA规范)

![image-20231007135809254](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007135809254.png)



**微服务架构下**

![image-20230928120543286](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230928120543286.png)

## Base理论

Base理论是对AP模式的扩展说明, 牺牲一致性, 换来可用性

**基本可用:**

分布式系统, 允许部分功能出现故障, 但不影响系统正常使用, 系统基本使用

**软状态:**

分布式事务中, 允许使用中间状态做标识, 一般不影响系统整体可用性

**最终一致性:**

分布式系统中, 允许节点中数据, 短时间内数据不一致, 但是数据最终要一致

## 分布式事务解决规范

+ 遵循CP(强一致性)
  + XA协议 - 需要数据库支持
+ 遵循AP(弱一致性)
  + TCC协议
  + AT
  + Saga
  + 可靠消息最用一致性

## 分布式事务解决规范 DTP

+ AP (Application) 应用程序
+ TM (Transaction Manager) 事务管理器
+ RM (Resource Manager) 资源管理器 - 理解为数据库

## 两阶段提交

事务的提交分为2个阶段

1. Prepare阶段 , TM（事务管理器）通知各个RM（资源管理器）准备提交它们的事务分支
2. Commit rollback阶段 - TM根据阶段1各个RM prepare的结果，决定是提交还是回滚事务

![image-20230928120852545](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230928120852545.png)

**两阶段提交方案下全局事务的ACID特性，是依赖于RM的。**一个全局事务内部包含了多个独立的事务 分支，这一组事务分支要么都成功，要么都失败。**各个事务分支的ACID特性共同构成了全局事务的 ACID特性。**也就是将单个事务分支支持的ACID特性提升一个层次到分布式事务的范畴

**2PC的问题:**

+ 同步阻塞问题
  + 第二阶段结束前, 数据库连接一直持有并不释放
+ 单点故障
  + 一旦事务管理器出现故障, RM会一直阻塞下去
+ 数据不一致
  + 若出现单点故障或网络问题, 第二阶段部分参与者收到commit请求, 部分参与者没有收到commit请求

## XA协议

> 强一致性的分布式事务解决方案

性能较低, 适合于金融行业, 并发量不大的场景下, 不存在中间状态

一个数据库的事务和多个数据库间的XA事务性能对比可发现，性能差10倍左右

**基本语法规范:**

+ xa_start          开始XA
+ xa_end           结束XA
+ xa_prepare    进入Prepare阶段
+ xa_commit     提交
+ xa_rollback     回滚

### 原生XA实现

实现复杂, 基本不使用

```java
public class JdbcTest {
    /* XA */
    public static void main(String[] args) throws SQLException, XAException {
        String ip = System.getenv("ENV_CLOUD_IP");
        String dbName1 = "distributed_transaction1";
        String dbName2 = "distributed_transaction2";
        String username = "root";
        String password = "123456";

        MysqlXid mysqlXid1 = new MysqlXid("111".getBytes(), "333".getBytes(), 1);
        MysqlXid mysqlXid2 = new MysqlXid("222".getBytes(), "444".getBytes(), 1);

        String url1 = "jdbc:mysql://" + ip + ":3306/" + dbName1 + "?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8";
        String url2 = "jdbc:mysql://" + ip + ":3306/" + dbName2 + "?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8";

        MysqlXADataSource xaDataSource1 = new MysqlXADataSource();
        xaDataSource1.setUrl(url1);
        xaDataSource1.setUser(username);
        xaDataSource1.setPassword(password);

        MysqlXADataSource xaDataSource2 = new MysqlXADataSource();
        xaDataSource2.setUrl(url2);
        xaDataSource2.setUser(username);
        xaDataSource2.setPassword(password);

        // 获取资源管理器 DB1
        XAConnection xaConnection1 = xaDataSource1.getXAConnection();
        XAResource xaResource1 = xaConnection1.getXAResource();

        // 获取资源管理器 DB2
        XAConnection xaConnection2 = xaDataSource2.getXAConnection();
        XAResource xaResource2 = xaConnection2.getXAResource();

        try{
            /* 数据库 1 */
            // 开启XA事务
            xaResource1.start(mysqlXid1,XAResource.TMNOFLAGS);
            // 执行SQL
            String sql1 = "update account set money=money-100 where id=1";
            xaConnection1.getConnection().prepareStatement(sql1).execute();
            // 关闭XA事务
            xaResource1.end(mysqlXid1,XAResource.TMSUCCESS);

            /* 数据库 2 */
            // 开启XA事务
            xaResource2.start(mysqlXid2,XAResource.TMNOFLAGS);
            // 执行SQL
            String sql2 = "update account set money=money+100 where id=2";
            xaConnection2.getConnection().prepareStatement(sql2).execute();
            // 关闭XA事务
            xaResource2.end(mysqlXid2,XAResource.TMSUCCESS);
            
            // Prepare 阶段
            int prepare1 = xaResource1.prepare(mysqlXid1);
            int prepare2 = xaResource2.prepare(mysqlXid2);
            
            if(prepare1 == XAResource.XA_OK && prepare2 == XAResource.XA_OK){
                xaResource1.commit(mysqlXid1,false);
                xaResource2.commit(mysqlXid2,false);
            }else{
                xaResource1.rollback(mysqlXid1);
                xaResource2.rollback(mysqlXid2);
            }
        }catch (Exception e){
            e.printStackTrace();
            xaResource1.rollback(mysqlXid1);
            xaResource2.rollback(mysqlXid2);
        }
    }
}
```

## Seata

在AP/TM/RM的基础上, 增加了`TC(事务协调者)`

TC是Seata的服务端, 是一个独立的应用程序, TM和RM都会和TC进行通信

![image-20231007142232813](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007142232813.png)

> 其中，TC 为单独部署的 Server 服务端，TM 和 RM 为嵌入到应用中的 Client 客户端。

### 声明周期

1. TM请求TC开启一个全局事务, TC 会生成一个 XID 作为该全局事务的编号
2. RM请求TC将本地事务注册为全局事务的分支事务 , 通过全局事务的 XID 进行关联
3. TM请求TC告诉XID对应的全局事务是进行提交还是回滚
4. TC驱动RM将XID对应的本地事务进行提交还是回滚

![image-20231007142842316](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007142842316.png)

### TC

**Server端存储模式分为三种:**

+ file：单机模式，性能最好 
+ db：高可用模式，全局事务会话信息通过db共享，相应性能差些 
+ redis：性能较高,存在事务信息丢失风险

**资源目录:**

https://github.com/seata/seata/tree/v1.6.1/script

![image-20231007143354436](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007143354436.png)

+ client : 存放client端sql脚本，参数配置

+ config-center:  各个配置中心参数导入脚本，config.txt(包含server和client)为通用参数文件

+ server: server端数据库脚本及各个容器配置

#### db存储模式+Nacos(注册&配置中心)

1. **[Seata下载](https://seata.io/en-us/blog/download.html)**

2. **创建Seata库**
   + 解压缩后, 找到 seata/script/server/db/mysql.sql导入到mysql中

![image-20231007144136631](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007144136631.png)

3. **配置Nacos注册中心**

> Seata的注册中心是作用于Seata自身的，和微服务自身配置的注册中心无关，但可以共用注册 中心

![image-20231007144637989](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007144637989.png)

以下注册中心都支持:

+ eureka
+ consul
+ `nacos`
+ etcd
+ zookeeper
+ sofa
+ redis
+ file

修改conf/application.yml文件

```yml
seata:
  registry:
    # support: nacos, eureka, redis, zk, consul, etcd3, sofa
    type: nacos
    nacos:
      application: seata-server
      server-addr: 127.0.0.1:8848
      group: SEATA_GROUP
      namespace: adb6f806-e08a-41c0-9160-f63d6ac6a732
      cluster: default
      username: nacos
      password: nacos
```

4. **配置Nacos配置中心**

> Seata的配置中心是作用于Seata自身的，和微服务自身配置的配置中心无关，但可以共用配置 中心

以下配置中心都支持:

+ `nacos`
+ consul
+ apollo
+ etcd
+ zookeeper
+ file

修改conf/application.yml文件

```yml
seata:
  config:
    # support: nacos, consul, apollo, zk, etcd3
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: adb6f806-e08a-41c0-9160-f63d6ac6a732
      group: SEATA_GROUP
      data-id: seataServer.properties
      username: nacos
      password: nacos																																																											
```

5. **上传配置到Nacos配置中心**

在Nacos中创建seataServer.properties

将seata/script/config-center/config.txt中的内容复制到seataServer.properties

修改为DB模式

```properties
store.mode=db
store.lock.mode=db
store.session.mode=db
store.db.driverClassName=com.mysql.jdbc.Driver
store.db.url=jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true&rewriteBatchedStatements=true
```

### 整合SpringCloud

```xml
<dependency>
    <groupId>com.alibaba.com</groupId>
    <artifactId>>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

AT模式下, 每个使用Seata的数据库均要创建一个undo_log表: [undo_log.sql](https://github.com/seata/seata/blob/v1.6.1/script/client/at/db/mysql.sql)

**application.yml**

```yaml
seata:
  # 默认AT
  data-source-proxy-mode: AT
  application-id: ${spring.application.name}
  # seata 服务分组，要与服务端配置service.vgroup_mapping的后缀对应
  tx-service-group: default_tx_group
  registry:
    type: nacos
    nacos:
      application: seata-server
      server-addr: 127.0.0.1:8848
      namespace: adb6f806-e08a-41c0-9160-f63d6ac6a732
      group: SEATA_GROUP
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: adb6f806-e08a-41c0-9160-f63d6ac6a732
      group: SEATA_GROUP
      data-id: seataServer.properties
```

**在全局事务发起者中添加@GlobalTransactional注解**

```java
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper,Order> implements OrderService{

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StockFeignService stockFeignService;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void createOrder() {
        // 创建订单
        Order order = new Order();
        order.setOrderName(UUID.randomUUID().toString());
        orderMapper.insert(order);
        // 调用其他服务, 扣减库存
        stockFeignService.discount();
    }
}
```

### Seata AT模式的设计思路

> Seata AT模式的核心是对业务无侵入，是一种改进后的两阶段提交

AT模式没有锁资源, 本地事务在一阶段直接提交, 同时生成前置镜像和后置镜像用于回滚

AT模式在二阶段执行完前, 存在全局行锁(select for update), 下一个事务在获取到全局锁之前, 无法执行事务

> 存在行锁, 不适合高并发场景(秒杀)

**一阶段:**

Seata AT模式的核心是对业务无侵入，是一种改进后的两阶段提交

![image-20231007223329438](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007223329438.png)

**二阶段:**

分布式事务操作成功，则TC通知RM异步删除undolog

![image-20231007223534094](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007223534094.png)

分布式事务操作失败，TM向TC发送回滚请求，RM 收到协调器TC发来的回滚请求，通过 XID 和 Branch ID 找到相应的回滚日志记录，通过回滚记录生成反向的更新 SQL 并执行，以完成分支的回滚

![image-20231007223601119](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20231007223601119.png)

### Seata XA



### Seata TCC









### 模式选择

|      |      |      |
| ---- | ---- | ---- |
|      |      |      |
|      |      |      |
|      |      |      |





## TODO 

vgroupMapping

1710618030885576705

## 单体项目多数据源

```xml
<dependencies>
	<dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.26</version>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>
</dependencies> 
```

```yaml
spring:
  datasource:
    db1:
      url: jdbc:mysql://${ENV_CLOUD_IP}:3306/distributed_transaction1?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver
    db2:
      url: jdbc:mysql://${ENV_CLOUD_IP}:3306/distributed_transaction2?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver
```

```java
@Configuration
@MapperScans({
        @MapperScan(basePackages = {"com.maple.seata.mapper1"},sqlSessionFactoryRef = "sqlSessionFactory1"),
        @MapperScan(basePackages = {"com.maple.seata.mapper2"},sqlSessionFactoryRef = "sqlSessionFactory2")
})
public class DBConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.db1")
    public DataSource dataSource1(){
        return new DruidDataSource();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DataSource dataSource2(){
        return new DruidDataSource();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory1(@Qualifier("dataSource1") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory2(@Qualifier("dataSource2") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }
}
```

```java
// com.maple.seata.mapper1
@Mapper
public interface Account1Mapper extends BaseMapper<Account> {

    @Update("update account set money=money-#{money} where id = 1")
    void reduceMoney(double money);
}
```

```java
// com.maple.seata.mapper2
@Mapper
public interface Account2Mapper extends BaseMapper<Account> {

    @Update("update account set money=money+#{money} where id = 2")
    void addMoney(double money);
}
```

### Seata-AT实现

```yaml
seata:
  registry:
    type: file # 注册中心, 默认file
  config:
    type: file # 配置中心, 默认file
  service:
    # 指定事务分组
    # default_tx_group 表示分组名称
    # default 表示Seata集群名称
    vgroup-mapping:
      default_tx_group: default
    # 指定服务器地址
    grouplist:
      default : 127.0.0.1:8091
    # 是否开启全局事务
    disable-global-transaction: false
  # 项目名称, 默认取值 spring.application.name
  application-id: seata-demo
  # 分组名
  tx-service-group: default_tx_group
  # 开启自动代理, 默认true
  enable-auto-data-source-proxy: true
  # 指定使用的模式
  data-source-proxy-mode: AT
```

```java
@Service
public class XAMultService {

    @Autowired
    private Account1Mapper account1Mapper;

    @Autowired
    private Account2Mapper account2Mapper;

    @GlobalTransactional(rollbackFor = Exception.class)
    public void operationMoney1(){
        this.account1Mapper.reduceMoney(200);
        int i = 10/0;
        this.account2Mapper.addMoney(200);
    }
}
```

### Seata-XA实现

```yaml
seata:
  registry:
    type: file # 注册中心, 默认file
  config:
    type: file # 配置中心, 默认file
  service:
    # 指定事务分组
    # default_tx_group 表示分组名称
    # default 表示Seata集群名称
    vgroup-mapping:
      default_tx_group: default
    # 指定服务器地址
    grouplist:
      default : 127.0.0.1:8091
    # 是否开启全局事务
    disable-global-transaction: false
  # 项目名称, 默认取值 spring.application.name
  application-id: seata-demo
  # 分组名
  tx-service-group: default_tx_group
  # 开启自动代理, 默认true
  enable-auto-data-source-proxy: true
  # 指定使用的模式
  data-source-proxy-mode: XA
```

```java
@Service
public class XAMultService {

    @Autowired
    private Account1Mapper account1Mapper;

    @Autowired
    private Account2Mapper account2Mapper;

    @GlobalTransactional(rollbackFor = Exception.class)
    public void operationMoney1(){
        this.account1Mapper.reduceMoney(200);
        int i = 10/0;
        this.account2Mapper.addMoney(200);
    }
}
```

## 分库分表

> 使用Sharding-JDBC实现分库分表

```xml
<dependencies>
	<dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.26</version>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
        <version>5.1.1</version>
    </dependency>
</dependencies> 
```









### 2PC问题





秒杀不要用AT


金融领域 TCC

电商 MQ





AT

![image-20230928122553767](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230928122553767.png)

SAGA

![image-20230928123408809](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230928123408809.png)

XA

![image-20230928123729037](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230928123729037.png)
