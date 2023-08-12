# Sharding JDBC - 分库分表

> 单表行数超过 500 万行或者单表容量超过 2GB，才推荐进行分库分表

> 如果预计三年后的数据量根本达不到这个级别，请不要在创建表时就分库分表

第一要务: 保证数据均匀, 保证单表数据量在一定范围内

## 优势

+ 提高系统性能
+ 提高系统可用性
+ 提高系统可扩展性
+ 提高系统灵活性
+ 降低系统硬件成本

## 需要考虑的问题

+ 主键生成策略
+ 数据备份
+ 数据迁移
+ 分布式事务


## Mysql服务搭建

+ [单机Docker部署](https://github.com/chenqf/material/tree/main/01.Deploy/mysql#%E5%8D%95%E6%9C%BA%E5%AE%89%E8%A3%85)
+ [主从Docker部署](https://github.com/chenqf/material/tree/main/01.Deploy/mysql#%E4%B8%BB%E4%BB%8E%E9%9B%86%E7%BE%A4)

## ShardingSphere 核心概念

+ 虚拟库 : 应用程序只需要像操作单数据源一样访问这个ShardingSphereDatasource即可
+ 真实库 : 实际保存数据的数据库
+ 逻辑表 : 应用程序直接操作的逻辑表
+ 真实表 : 实际保存数据的表
+ 分布式主键生成算法 
+ 分片策略


## 拆分方式

+ 垂直分库 - 把单一数据库, 按照业务进行划分, 专库专表 TODO 跨库级联查询
+ 垂直分表 - 业务表拆分为`主表`(主要字段)+`附表`(非常用信息)
  + 避免单表占用磁盘过多/IO过大
  + 修改字段时, 锁表或锁行时影响更少
+ 水平分库 - 表中数据过大, 使用多个数据库来进行分担, 每个数据库结构相同
+ 水平分表 - 表中数据量过大, 使用多个表进行分担, 每个表结构相同

### 基本配置

```xml
 <dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>5.1.1</version>
</dependency>
```

**配置多数据源:**
```yaml
spring:
  shardingsphere:
    datasource:
      sharding:
        default-data-source-name: m0
      names: m0,m1,m2
      m0:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://${ENV_CLOUD_IP}:3306/demo?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
        username: root
        password: 123456
      m1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://${ENV_CLOUD_IP}:3307/sharding_1?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
        username: root
        password: 123456
      m2:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://${ENV_CLOUD_IP}:3307/sharding_2?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
        username: root
        password: 123456
```

**指定分片表拆分情况**

```yaml
spring:
  shardingsphere:
    rules:
      sharding:
        tables:
          user: # 逻辑表名: user
            actual-data-nodes: m0.user #真实表: m0库中的user表
          item: # 逻辑表名: item
            actual-data-nodes: m1.item #真实表: m1库中的item表
          course: # 逻辑表名: course
            actual-data-nodes: m$->{[1,2]}.course_$->{0..2} #真实表: m1库中的course_0,course_1,course2 和 m2库中的course_0,course_1,course2
```

### 分布式主键

建议使用雪花算法, 不同进程主键不重复, 相同进程主键有序性

须避免时间回溯, 保证主键不重复

#### 使用默认雪花算法

```yaml
spring:
  shardingsphere:
    rules:
      sharding:
        key-generators:
          # 雪花算法-自定义策略名: my_id_for_snowflake_alg
          my_id_for_snowflake_alg:
            type: SNOWFLAKE
            props:
              worker:
                id: 1
        # 默认所有id都使用my_id_for_snowflake_alg策略
        default-key-generate-strategy:
          column: id
          key-generator-name: my_id_for_snowflake_alg
```

#### 自定义雪花算法

TOOD

### 分片实现

每个`逻辑表`, 仅支持`一种数据库分片策略`和`一种表分辨策略`

若想对同一组`实体表`实现多种策略, 可创建多个逻辑表, 对每个逻辑表配置不同的分片策略

实际工作中, 要极力避免全分片表扫描

#### STANDARD 

简单表单式 ,基于一个`分片键`的分片策略

```yaml
spring:
  shardingsphere:
    rules:
      sharding:
        sharding-algorithms:
          db_standard_alg: # db分片规则
            type: INLINE
            props:
              algorithm-expression: m$->{id % 2 + 1} # 根据id判断DB m1 / m2
              allow-range-query-with-inline-sharding: true
          table_standard_alg: # table分片规则
            type: INLINE
            props:
              algorithm-expression: course_$->{id % 3} # 根据id判断Table course_0 / course_1 / course_2
              allow-range-query-with-inline-sharding: true
        tables:
          course:
            actual-data-nodes: m$->{[1,2]}.course_$->{0..2}
            database-strategy:
              standard:
                sharding-column: id
                sharding-algorithm-name: db_standard_alg
            table-strategy:
              standard:
                sharding-column: id
                sharding-algorithm-name: table_standard_alg
```

查询时, 可通过`分片键`进行精准匹配, 也可使用`IN`对`分片键`进行查询

查询时, 默认不支持范围查询, 须配置 `allow-range-query-with-inline-sharding: true`

若查询时, 未有分片键的精准匹配, 会进行全分片表的查询

#### COMPLEX

复杂表达式, 基于多个`分片键`的分片策略

```yaml
spring:
  shardingsphere:
    rules:
      sharding:
        sharding-algorithms:
          db_complex_alg:
            type: COMPLEX_INLINE
            props:
              algorithm-expression: m$->{(id + user_id) % 2 + 1}
          table_complex_alg:
            type: COMPLEX_INLINE
            props:
              algorithm-expression: course_$->{(id + user_id) % 3}
        tables:
          course:
            actual-data-nodes: m$->{[1,2]}.course_$->{0..2}
              database-strategy:
                complex:
                  sharding-columns: id,user_id
                  sharding-algorithm-name: db_complex_alg
              table-strategy:
                complex:
                  sharding-columns: id,user_id
                  sharding-algorithm-name: table_complex_alg
```

查询时, 不指定分片键则`全分片表`查询, 若`指定分片键`, 则`所有分片键`必须全部参与查询, 否则报错

#### LINT

强制分片算法, 在代码中强制指定当前的分片

```yaml
spring:
  shardingsphere:
    rules:
      sharding:
        sharding-algorithms:
          db_hint_alg:
            type: HINT_INLINE
            props:
              algorithm-expression: m$->{value} # 必须使用value关键字
          table_hint_alg:
            type: HINT_INLINE
            props:
              algorithm-expression: course_$->{value} # 必须使用value关键字
        tables:
          course:
            actual-data-nodes: m$->{[1,2]}.course_$->{0..2}
              database-strategy:
                hint:
                sharding-algorithm-name: db_hint_alg
              table-strategy:
                hint:
                sharding-algorithm-name: table_hint_alg
```

**使用:**

```java
@RestController
@RequestMapping("/demo")
public class DemoController {

  @Autowired
  private CourseMapper courseMapper;

  @GetMapping("/getAllByHint")
  public List<Course> getAllByHint(){
    @Cleanup HintManager hintManager = HintManager.getInstance();
    hintManager.addDatabaseShardingValue("course",2); // m2
    hintManager.addTableShardingValue("course",1); // course_1
    List<Course> courses = this.courseMapper.selectList(null);
    return courses;
  }
}
```

#### CLASS_BASE - 自定义分片算法

通过java类实现分片规则, 这个类必须是指定策略对应的算法接口实现类

+ STANDARD -> StandardShardingAlgorithm;
+ COMPLEX -> ComplexKeysShardingAlgorithm;
+ HINT -> HintShardingAlgorithm

配置方式-基于STANDARD:

```yaml
spring:
  shardingsphere:
    rules:
      sharding:
        sharding-algorithms:
          db_class_base_standard_alg:
            type: CLASS_BASED
            props:
              strategy: STANDARD
              algorithmClassName: com.maple.sharding.algorithm.MyDbStandardAlgorithm
          table_class_base_standard_alg:
            type: CLASS_BASED
            props:
              strategy: STANDARD
              algorithmClassName: com.maple.sharding.algorithm.MyTbStandardAlgorithm
        tables:
          course:
            actual-data-nodes: m$->{[1,2]}.course_$->{0..2}
              database-strategy:
                standard:
                  sharding-algorithm-name: db_class_base_standard_alg
                  sharding-column: id
              table-strategy:
                standard:
                  sharding-algorithm-name: table_class_base_standard_alg
                  sharding-column: id
```

**表分片类:**
```java
public class MyTbStandardAlgorithm implements StandardShardingAlgorithm {
    /**
     * 精确匹配时, 进入该方法
     * 返回对应的table
     */
    @Override
    public String doSharding(Collection collection, PreciseShardingValue preciseShardingValue) {
        // collection : {course_0,course_1,course_2}
        // preciseShardingValue : 查询的精确值
        return "course_0";
    }

    /**
     * 范围匹配, 进入该方法
     * 返回对应的多个table
     */
    @Override
    public Collection<String> doSharding(Collection collection, RangeShardingValue rangeShardingValue) {
        // collection : {course_0,course_1,course_2}
        // rangeShardingValue : {logicTableName:course,columnName:id,valueRange:查询的范围值}
        return collection;
    }

    @Override
    public void init() {

    }

    @Override
    public String getType() {
        return null;
    }
}
```
**库分片类:**
```java
public class MyDbStandardAlgorithm implements StandardShardingAlgorithm {
    /**
     * 精确匹配时, 进入该方法
     * 返回对应的db
     */
    @Override
    public String doSharding(Collection collection, PreciseShardingValue preciseShardingValue) {
        // collection : {m1,m2}
        // preciseShardingValue : {logicTableName:course,columnName:id,value:查询的精确值}
        return "m1";
    }

    /**
     * 范围匹配, 进入该方法
     * 返回对应的多个db
     */
    @Override
    public Collection<String> doSharding(Collection collection, RangeShardingValue rangeShardingValue) {
        // collection : {m1,m2}
        // rangeShardingValue : {logicTableName:course,columnName:id,valueRange:查询的范围值}
        return collection;
    }

    @Override
    public void init() {

    }

    @Override
    public String getType() {
        return null;
    }
}
```

### 读写分离

> 将数据库拆分为主库和从库，主库负责处理事务性的增删改操作，从库负责处理查询操作，能够有效的避免由数据更新导致的行锁，使得整个系统的查询性能得到极大的改善
> 
> 通过一主多从的配置方式，可以将查询请求均匀的分散到多个数据副本，能够进一步的提升系统的处理能力

通过SQL语义分析, 实现读写分离, 将读写发送到不同的数据库中

```yaml
spring:
  shardingsphere:
    datasource:
      sharding:
        default-data-source-name: m1
      names: m1,m2,s1,s2
      m1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://${ENV_CLOUD_IP}:3307/sharding_1?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
        username: root
        password: 123456
      m2:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://${ENV_CLOUD_IP}:3307/sharding_2?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
        username: root
        password: 123456
      s1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://${ENV_CLOUD_IP}:3308/sharding_1?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
        username: root
        password: 123456
      s2:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://${ENV_CLOUD_IP}:3308/sharding_2?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
        username: root
        password: 123456
    rules:
      readwrite-splitting:
        data-sources:
          myds1:
            type: Static
            props:
              write-data-source-name: m1
              read-data-source-names: s1
            load-balancer-name: alg_round
          myds2:
            type: Static
            props:
              write-data-source-name: m2
              read-data-source-names: s2
            load-balancer-name: alg_round
        load-balancers:
          alg_round:
            type: ROUND_ROBIN
```

**若在读请求中强制使用主库:**
```java
@RestController
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/getUser")
    public List<User> getUser(){
  
      HintManager instance = HintManager.getInstance(); // 获取管理器
      instance.setWriteRouteOnly(); // 强制使用主库
  
      List<User> users = this.userMapper.selectList(null);
  
      instance.setReadwriteSplittingAuto(); // 恢复自动读写分离
  
      return users;
    }
}
```

**通过注解实现:**

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ShardingMasterOnly {
}
```

```java
@Component
@Aspect
@Slf4j
public class ShardingMasterAspect {
    @Pointcut("@annotation(com.maple.sharding.aop.ShardingMasterOnly)")
    public void cutPointcut() {}
    
    @Around("cutPointcut()")
    public Object masterOnly(ProceedingJoinPoint joinPoint) throws Throwable {
        HintManager instance = HintManager.getInstance();
        try{
            instance.setWriteRouteOnly();
            return joinPoint.proceed();
        }finally {
            instance.setReadwriteSplittingAuto();
            instance.close();
        }
    }
}
```

### 关联表

避免跨库关联, 关联表放到一个库中

### 公共表 (字典表)

存储固定数据的表,表数据很少发生变化, 查询时经常进行关联
在每个数据库中创建相同结构的公共表

### 主键生成

雪花算法: 不同进程主键不重复, 相同进程主键有序性


## 分片策略



**INLINE - 单一键表达式**

查询时, 可基于分片键==查询, 或in查询

+ 尽量不要用in (id1,id2,id3) , 确保id都在一个分片中, 否则会`全分片表`扫描
+ 无法使用between查询, 需要单独配置允许范围查询 - 全分片表扫描

**COMPLEX_INLINE - 复杂表达式**

**CLASS_BASE - 自定义复杂逻辑**

定制一个复杂的类实现分片策略





----------------------------------------

每写一个sql都要考虑sql是怎么执行的

避免虚拟列查询, 会导致全分片查询

第一要务, 表数据分配均匀

时间分片,数据峰值不固定






海量数据场景下, 不要使用存储过程


可分多个策略, 一个策略用于插入, 其他策略用于查询


mp 字段名尽量不要用id, 用了id, 默认吧id作为主键, 默认生成雪花算法, 不会将id交由shardingJDBC处理

避免虚拟列进行查询, 虚拟列导致全分片  路由


distinct查询