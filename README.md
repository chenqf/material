
> 本项目用于记录整理java相关组件的部署及技术实现, 每个子项目分别对应若干技术组件, 详见下方目录

+ [nacos](../../tree/main/spring-cloud-alibaba-user)
  + docker下集群部署及数据持久化 
  + [注册中心](../../tree/main/spring-cloud-alibaba-user#注册中心)
  + [配置中心](../../tree/main/spring-cloud-alibaba-user#配置中心)
  + [整合OpenFeign](../../tree/main/spring-cloud-alibaba-user#nacos注册中心结合openfeign)
+ [sentinel](../../tree/main/spring-cloud-alibaba-stock)
+ [redis](../../tree/main/redis-demo)
+ [cache](../../tree/main/cache-demo)
+ [rabbitMQ](../../tree/main/rabbitMQ-demo)
+ [任务](../../tree/main/task-demo)
  + [异步任务](../../tree/main/task-demo#异步任务)
  + [单机定时任务](../../tree/main/task-demo#单机定时任务)
  + 集群定时任务
  + 动态时间定时任务
  + [邮件任务](../../tree/main/task-demo#邮件任务)
+ 模版引擎
+ spring security
  + 应用场景
+ [Swagger](../../tree/main/swagger-demo)
+ 布隆过滤器
+ JUC
+ CAP设计方案
+ 分布式ID
+ 并发编程, https://www.bilibili.com/video/BV1EZ4y147qZ/?spm_id_from=333.337.search-card.all.click
+ 滑动窗口 jdhotkey
+ 六边形架构
+ 整洁架构






在内部@Bean没有依赖的情况下, 建议配置, 性能更好,启动更快
@Configuration(proxyBeanMethods = false) // 就相当于以前的bean.xml
@EnableAutoConfiguration
  @AutoConfigurationPackage
@import


批处理





