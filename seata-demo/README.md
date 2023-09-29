# 分布式事务

`CAP理论(布鲁尔定理)`: 分布式事务中无法同时满足 Consistency / Availability / Partition tolerance

+ `C: 一致性`, 数据操作要么全部成功, 要么全部失败
+ `A: 可用性` , 各个节点必须可用, 客户端访问时, 不能出现等待/超时/报错
+ `P: 分区容错性`, 多个节点, 就算节点之间通信失败, 也能正常对外提供服务

> 解决分布式事务, 遵循CP或者AP

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

## 分布式事务解决规范

+ AP (Application) 应用程序
+ TM (Transaction Manager) 事务管理器
+ RM (Resource Manager) 资源管理器 - 理解为数据库

## 两阶段提交

事务的提交分为2个阶段

1. Prepare阶段 , TM（事务管理器）通知各个RM（资源管理器）准备提交它们的事务分支
2. Commit rollback阶段 - TM根据阶段1各个RM prepare的结果，决定是提交还是回滚事务

![image-20230928120852545](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230928120852545.png)

**两阶段提交方案下全局事务的ACID特性，是依赖于RM的。**一个全局事务内部包含了多个独立的事务 分支，这一组事务分支要么都成功，要么都失败。**各个事务分支的ACID特性共同构成了全局事务的 ACID特性。**也就是将单个事务分支支持的ACID特性提升一个层次到分布式事务的范畴

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











### 2PC问题

一直持有连接,协调者通知提交或回滚, 才会释放连接, 降低并发
网络问题, 数据不一致
协调者单点问题



秒杀不要用AT


金融领域 TCC

电商 MQ





AT

![image-20230928122553767](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230928122553767.png)

SAGA

![image-20230928123408809](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230928123408809.png)

XA

![image-20230928123729037](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230928123729037.png)
