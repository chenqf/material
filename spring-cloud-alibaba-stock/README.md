# Sentinel

实现原理: 客户端通过SpringMVC 拦截器实现

> 服务器挂掉的原因
+ 流量激增,CPU负载升高,无法正常处理请求
+ 激增流量打垮冷系统(数据库连接未创建, 缓存未预热)
+ 消息投递速度过快, 消息处理积压
+ 慢SQL查询卡爆连接池
+ 第三方服务不响应, 卡爆连接池
+ 业务异常未处理, 产生副作用
+ 其他

> 什么是服务雪崩 ? 
> 
> 因服务提供者的不可用导致服务调用者的不可用, 并将不可用逐渐放大的过程

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230703165329524.png)

## 部署

TODO
+ docker集群
+ k8s集群

> sentinel-dashboard jar包下载:
> 
> https://github.com/alibaba/Sentinel/releases

```shell
# 启动 sentinel-dashboard
# 指定 端口:8858 用户名:sentinel 密码:123456
java -Dsentinel.dashboard.auth.username=sentinel -Dsentinel.dashboard.auth.password=123456 -Dserver.port=8858 -jar sentinel-dashboard-1.8.6.jar
```

> sentinel-dashboard 访问地址:
> 
> http://127.0.0.1:8858/

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230707234959800.png)

## 结合Spring-Cloud-Alibaba使用

#### pom.xml
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

#### application.yml
```yaml
spring:
  application:
    name: spring-cloud-alibaba-stock 
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8858
      web-context-unify: false # 默认true调用链路收敛, false将调用链路展开
```

#### sentinel 统一容错处理

```java
@Component
public class MyBlockExceptionHandler implements BlockExceptionHandler {

    private  final Logger logger = LoggerFactory.getLogger(MyBlockExceptionHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        logger.info("BlockExceptionHandler BlockException===========" + e.getRule());
        Result r = null;
        if(e instanceof FlowException){
            r = Result.error(100,"接口限流了");
        } else if(e instanceof DegradeException){
            r = Result.error(101,"服务降级了");
        }else if(e instanceof ParamFlowException){
            r = Result.error(102,"热点参数限流了");
        }else if(e instanceof SystemBlockException){
            r = Result.error(103,"触发系统保护规则了");
        }else if(e instanceof AuthorityException){
            r = Result.error(104,"授权规则不通过");
        }
        response.setStatus(500);
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getWriter(),r);
    }
}
```

> 统一容错处理, 对于使用@SentinelResource定义的资源无效, 须单独指定容错方法

#### @SentinelResource定义资源

> 可在controller的方法中定义, 也可以在其他任意方法上定义
> 
> 须单独指定容错方法

```java
@Service
public class TestServiceImp implements TestService {

    private  final Logger logger = LoggerFactory.getLogger(TestServiceImp.class);

    @Override
    @SentinelResource(value = "random",blockHandler = "randomBlockExceptionHandler")
    public Integer random() {
        return (int)Math.round(Math.random() * 10);
    }

    public Integer randomBlockExceptionHandler(BlockException e) throws BlockException {
        logger.info("BlockExceptionHandler BlockException===========" + e.getRule());
        return null;
    }
}
```

## sentinel使用nacos做持久化 (Push 模式)

![image-20230708000746995](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230708000746995.png)

