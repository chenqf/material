# 事务

**什么是事务:** 将多个数据库操作打包成一个`不可分割`的整理来进行执行

```sql
start transaction; -- 开启事务

-- 若干sql操作

commit; -- 提交事务
```

## ACID

最终要保证`一致性`, 其他特性都是为一致性服务的

![image-20230927151605166](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230927151605166.png)

### 原子性

> 操作要么全成功, 要么全失败

![image-20230927152403261](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230927152403261.png)

### 隔离性

> 事务执行过程中相互隔离, 事务之间不能相互干扰, 不能查看未提交的数据

![image-20230927152514047](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230927152514047.png)

#### 引申问题

**脏读 : ** `事务A`读取到了`事务B`还未提交的数据

![image-20230927152907794](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230927152907794.png)

**不可重复读: **A事务执行过程中, 对同一条数据进行2次读取, 2次读取之间, B事务修改了数据并进行了提交, A事务读取到了不同的数据

![image-20230927152946374](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230927152946374.png)

**幻读:** A事务对同一个集合数据进行2次读取, 2次读取之间, B事务在集合中新增或删除了部分数据, A事务2次读取到了数量不一致的行数据

![image-20230927153245302](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230927153245302.png)

#### 事务隔离级别

![image-20230927153516402](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230927153516402.png)

```sql
SHOW VARIABLES LIKE 'transaction_isolation'; -- 显示当前隔离界别, 默认-REPEATABLE-READ
SET transaction_isolation = 'REPEATABLE-READ'; -- 设置隔离界别
```

### 持久性

> 事务一旦提交, 事务的更改, 就不会由于电源故障/系统崩溃等意外而发生变化

![image-20230927154024473](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230927154024473.png)

### 一致性

> 原子性 / 隔离性 / 持久性 都满足后,  一致性就满足了

![image-20230927154054956](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230927154054956.png)

## 代码实现事务

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

### JDBC实现事务

```java
public class JdbcTest {
    /**
     * 最普通的事务
     */
    public static void main(String[] args) throws SQLException {
        String ip = System.getenv("ENV_CLOUD_IP");
        String dbName = "standalone_transaction";
        String username = "root";
        String password = "123456";
        String url = "jdbc:mysql://" + ip + ":3306/" + dbName + "?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8";
        System.out.println(url);
        Connection connection = DriverManager.getConnection(url, username, password);

        //开启事务
        connection.setAutoCommit(false);
        try{
            String sql1 = "update account set money=money-100 where id=1";
            connection.prepareStatement(sql1).execute();
            String sql2 = "update account set money=money+100 where id=2";
            connection.prepareStatement(sql2).execute();

            // 提交事务
            connection.commit();
        }catch (Exception e){
            // 事务回滚
            connection.rollback();
        }
    }
}
```

### Spring编程式事务
```java
@Service
public class DemoService {

    @Autowired
    private AccountMapper accountMapper;

    private final TransactionTemplate transactionTemplate;

    public DemoService(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void manualTransaction(){
        transactionTemplate.execute(status -> {
            try{
                this.dbOperation();
            }catch (Exception e){
                status.setRollbackOnly();
                throw e;
            }
            return null;
        });

    }

    public void dbOperation(){
        Account account1 = accountMapper.selectById(1L);
        Account account2 = accountMapper.selectById(2L);

        account1.setMoney(account1.getMoney() - 100);
        account2.setMoney(account2.getMoney() + 100);

        accountMapper.updateById(account1);
        accountMapper.updateById(account2);
    }
}
```

### Spring声明式事务

```java
@Service
public class DemoService {

    @Autowired
    private AccountMapper accountMapper;

    @Transactional
    public void autoTransaction(){
        this.dbOperation();
    }

    public void dbOperation(){
        Account account1 = accountMapper.selectById(1L);
        Account account2 = accountMapper.selectById(2L);

        account1.setMoney(account1.getMoney() - 100);
        account2.setMoney(account2.getMoney() + 100);

        accountMapper.updateById(account1);
        accountMapper.updateById(account2);
    }
}
```

#### 声明式事务失效场景

1. 方法内部调用
    + `@Transactional`声明的方法只有代理对象调用时才生效,被代理对象来调用就会失效
        + 把方法放到另一个Bean中
        + 将自己注入自己, 用注入自己的方法来调用
        + 获取代理对象 `@EnableAspectJAutoProxy(exposeProxy = true)` / `XxxService proxy = (XxxService) AopContext.currentProxy()`
2. private方法
    + `cglib`时基于父子类来实现的, 子类不能重载父类的`private`方法
3. 方法被final修饰
    + 原因和private一样, 子类不能重写父类中的final方法
4. 单独线程调用SQL
    + 单独线程内的jdbc默认`autocommit=true`
5. spring项目非springBoot项目, 缺少@Configuration
6. 异常被吃掉, 抛出RuntimeException及其子类才会回滚
    + 异常被捕获没有抛出
    + 抛出的Exception不是RuntimeException及其子类 (@Transactional(rollbackFor=Exception.class))
7. 数据库不支持
8. 没有被spring管理