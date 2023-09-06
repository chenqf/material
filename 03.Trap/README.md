# 常见开发陷阱

## @Transactional

作用: 方法执行成功, 自动提交; 方法抛出RuntimeException及其子类时自动回滚

> `@Transactional`声明的方法只有代理对象调用时才生效,被代理对象来调用就会失效

### 何时失效

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

### 传播机制

TODO

## ThreadLocal

基于副本的隔离机制, 来保证共享变量修改的安全性

### 使用场景

1. 线程的上下文传递信息
   + 请求相关信息(用户信息/请求ID)存储到ThreadLocal, 后续请求的链路处理中方便的访问这些信息
2. 数据库的连接管理
   + 使用数据库连接池, 数据库连接存储到ThreadLocal, 每个线程独立管理自己的数据库连接, 避免线程间的竞争和冲突
   + 例子: Mybatis中的SqlSession存储当前线程的数据库会话信息
3. 事务管理
   + 手动事务管理的场景, 事务上下文存储到ThreadLocal, 每个线程控制自己的事务, 保证事务的隔离性

### 使用规范

## SQL: limit 500000,10 vs limit 10

速度差异:
数据量很大, limit 500000,10 需要跳过大量数据行


java 场景题

1. 如何判断一个对象是否实现了某个接口
   + instanceof
2. 