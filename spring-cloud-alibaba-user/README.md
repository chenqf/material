# Nacos

## 集群

![image-20230908163449367](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230908163449367.png)

![image-20230908162132905](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230908162132905.png)

> 使用VIP/nginx请求时，需要配置成TCP转发，不能配置http2转发，否则连接会被nginx断 开。 9849和7848端口为服务端之间的通信端口，请勿暴露到外部网络环境和客户端测。

**部署:**

1. 部署3台以上nacos作为一个集群
2. 将nacos连接到mysql做数据持久化
3. 使用nginx或HaProxy做负载均衡器

`TODO` Docker下部署:

**访问nacos管理界面**:

http://{hostname}:8848/nacos

登录 ，用户名和密码都是nacos

## Nacos逻辑隔离

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230707214600578.png)

**命名空间(Namespace) :** 用于进行租户粒度的隔离，Namespace 的常用场景之一是不同环境 的隔离

**服务分组(Group) :** 不同的服务可以归类到同一分组，一般用于区分不同的项目

## 临时实例和持久化实例

在定义上区分临时实例和持久化 实例的关键是健康检查的方式。

临时实例使用客户端上报模式， 而持久化实例使用服务端反向探测模式。

临时实例需要能够自动摘除不健康实例，而且无需持久化存储实例。

持久化实例使用服务端探测的健康检查方式，因为客户端不会上报心跳，所以不能自动摘除下线的实例。

> ⼀些基础的组件例如数据库、缓存等，这些往往 不能上报心跳，这种类型的服务在注册时，就需要作为持久化实例注册

## 注册中心

**什么是注册中心:**

![image-20230908161431528](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230908161431528.png)

**注册中心选型:**

![image-20230908161517614](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230908161517614.png)

**nacos注册中心架构:**

![image-20230908161628033](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230908161628033.png)

**核心功能:**

1. **服务注册:** Nacos Client会通过发送REST请求的方式向Nacos Server注册自己的服务，提供自身的元 数据，比如ip地址、端口等信息
2. **服务心跳:** 在服务注册后，Nacos Client会维护一个定时心跳来持续通知Nacos Server，说明服务一 直处于可用状态，防止被剔除。`默认5s发送一次心跳`
3. **服务同步：**Nacos Server集群之间会互相同步服务实例，用来保证服务信息的一致性。
4. **服务发现：**服务消费者（Nacos Client）在调用服务提供者的服务时，会发送一个REST请求给Nacos Server，获取上面注册的服务清单，并且缓存在Nacos Client本地，同时会在Nacos Client本地开启 一个定时任务定时拉取服务端最新的注册表信息更新到本地缓存
5. **服务健康检查：**Nacos Server会开启一个定时任务用来检查注册服务实例的健康情况，对于超过15s 没有收到客户端心跳的实例会将它的healthy属性置为false(客户端服务发现时不会发现)，如果某个实 例超过30秒没有收到心跳，直接剔除该实例(被剔除的实例如果恢复发送心跳则会重新注册)

### 微服务整合Nacos注册中心

#### 依赖 pom.xml

```xml
<!--   nacos 服务注册发现     -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

#### 配置 application.yml

更多配置： https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-discovery

```yaml
server:
  port: 8001
spring:
  application:
    name: spring-cloud-alibaba-user # 应用名称, nacos会将名称当做服务名称
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848 # nacos的服务地址
      discovery:
        username: nacos
        password: nacos
        namespace: adb6f806-e08a-41c0-9160-f63d6ac6a732 # 命名空间, 主要用于区分环境 dev/sit/prev/prod
        group: material # 组名称, 一般用于区分项目
```

### 整合RestTemplate+Spring Cloud LoadBalancer实现微服务调用

```xml
<!--  nacos 不再自带Ribbon,须单独引用springCloudLoadbalancer -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
<!--  spring-retry 用于服务间调用异常重试  -->
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
```

```yaml
spring:
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      discovery:
        username: nacos
        password: nacos
        # 命名空间, 主要用于区分环境 dev/sit/prev/prod
        namespace: adb6f806-e08a-41c0-9160-f63d6ac6a732
        # 组名称, 一般用于区分项目
        group: material
    loadbalancer:
      cache:
        # 启用本地缓存, 根据实际情况权衡
        enabled: true
        # 缓存空间大小
        capacity: 1000
        # 缓存的存活时间, 单位s
        ttl: 10
      health-check:
        # 重新运行运行状况检查计划程序的时间间隔
        interval: 25s
        # 运行状况检查计划程序的初始延迟值
        initial-delay: 30
      # 需要引入Spring Retry依赖
      retry:
        # 该参数用来开启重试机制，默认是关闭
        enabled: true
        # 切换实例的重试次数
        max-retries-on-next-service-instance: 2
        # 对当前实例重试的次数
        max-retries-on-same-service-instance: 0
        # 对所有的操作请求都进行重试
        retry-on-all-operations: true
        # Http响应码进行重试
        retryable-status-codes: 500,404,502,503
```

#### 使用RestTemplate

```java
@Configuration
public class RestConfig {
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }
}
```
```java
@RequestMapping("/restTemplate")
@RestController
public class RestTemplateController {

    @Autowired
    private RestTemplate template;

    @GetMapping("/stock")
    public Result demo(){
        System.out.println("尝试请求stock");
        // spring-cloud-alibaba-stock 为其他微服务在nacos中注册的应用名
        String url = "http://spring-cloud-alibaba-stock/stock/num";
        Result<Integer> r = this.template.getForObject(url, Result.class);
        return Result.success("user:chenqf;stock:" + r.getData());
    }
}
```

### 服务分级存储

注册中心的核心数据是服务的名字和它对应的网络地址

![image-20230911194830370](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911194830370.png)

对于服务实例, 可能存在多机房部署的, 那么可能需要对每个机 房的实例做不同的配置，这样又需要在服务和实例之间再设定⼀个数据级别

![image-20230911195449193](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911195449193.png)

**集群配置:**

```properties
spring.cloud.nacos.discovery.cluster-name=BJ
```

**自定义LoadBalancer配置机制实现优先同集群间调用**

```java
public class LoadBalancerConfig {
    @Bean
    ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory,
            NacosDiscoveryProperties nacosDiscoveryProperties) {

        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        return new NacosLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
                name,
                nacosDiscoveryProperties
        );
    }
}
```

**指定@LoadBalancerClient, 并指定对哪个服务使用**

```java
@SpringBootApplication
@LoadBalancerClient(value = "stock-service", configuration = LoadBalancerConfig.class)
public class UserApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(UserApplication.class, args);
    }
}
```

> 该算法也支持整合后的OpenFeign

## Nacos注册中心结合OpenFeign

Feign是Netflix开发的声明式、模板化的HTTP客户端，Feign可帮助我们更加便捷、优雅地调用HTTP API。

Feign可以做到使用 HTTP 请求远程服务时就像调用本地方法一样的体验，开发者完全感知不到 这是远程方法，更感知不到这是个 HTTP 请求。

Spring Cloud OpenFeign对Feign进行了增强，使其支持Spring MVC注解，从而使得Feign的使用更加方便。

![image-20230914122443180](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230914122443180.png)

**引入依赖：**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**添加@EnableFeignClients注解，开启openFeign功能：**

```java
@SpringBootApplication
@EnableFeignClients
public class UserApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(UserApplication.class, args);
    }
}
```

**编写OpenFeign客户端：**

```java
@FeignClient(name = "spring-cloud-alibaba-stock",path = "/stock")
public interface StockFeignService { // 该接口无需实现, 但方法返回值类型,注解,方法名要和被调用的服务controller一致
    @GetMapping("/num")
    Result<Integer> stock();
}
```

+ 返回值确保和被调用Controller中的返回值相同
+ 方法名随意
+ 参数: 使用对应的注解
  + 参数前如果没有注解, 默认添加@RequestBody, 最多只能存在一个不带注解的参数
  + @SpringQueryMap 用于接收多个query参数

**微服务调用者发起调用，像调用本地方式一样调用远程微服务提供者：**

```java
@RequestMapping("/feign")
@RestController
public class FeignController {

    @Autowired
    private StockFeignService stockFeignService;

    @GetMapping("/stock")
    public Result demo(){
        Result<Integer> r = this.stockFeignService.stock();
        return Result.success("user:chenqf;stock:" + r.getData());
    }
}
```

### OpenFeign扩展优化

### 日志配置：

+ NONE【性能最佳，默认值】：不记录任何日志。 
+ `BASIC【适用于生产环境追踪问题】`：仅记录请求方法、URL、响应状态代码以及执行时间。
+ HEADERS：记录BASIC级别的基础上，记录请求和响应的header。 
+ `FULL【比较适用于开发及测试环境定位问题】`：记录请求和响应的header、body和元数据。

**方式一:**

```java
@Configuration
public class FeignConfiguration {

    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.BASIC;
    }
}
```

openFeign的输入日志是debug的, 所以要修改对应包下的日志输入级别

```yaml
logging:
  level:
    com.maple.user.feign: debug # 指定该路径下的日志界别为debug
```

**方式二(优先级更高-建议使用):**

```yaml
feign:
  client:
    config:
      default: # 所有服务生效
        logger-level: FULL
      other-micro: # 针对单个微服务进行配置
        logger-level: FULL
logging:
  level:
    com.maple.user.feign: debug # 指定该路径下的日志界别为debug
```

### 超时时间配置

**方式一:**

```java
@Configuration
public class FeignConfiguration {
    @Bean
    public Request.Options options(){
        // connectTimeout : 连接超时时间
        // readTimeout : 连接建立后响应超时时间
        return new Request.Options(3000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS,true);
    }
}
```

****方式二(优先级更高-建议使用):****

```yaml
feign:
  client:
    config:
      default: # 所有服务生效
        # 连接超时时间
        connect-timeout: 3000
        # 请求处理超时时间
        read-timeout: 5000
      other-micro: # 针对单个微服务进行配置
        # 连接超时时间
        connect-timeout: 3000
        # 请求处理超时时间
        read-timeout: 5000
```

> Feign的底层用的是Ribbon或者LoadBalancer，但超时时间以Feign配置为准

### 替换HTTP请求组件

Feign 中默认使用 JDK 原生的 URLConnection 发送 HTTP 请求，没有连接池，我们可以集成别的组件来替换掉 URLConnection，比如 Apache HttpClient5，OkHttp。

> Feign发起调用真正执行逻辑：feign.Client#execute

**配置Apache HttpClient5**

```xml
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-hc5</artifactId>
</dependency>
```

```yaml
feign:
  httpclient:
    hc5:
      enabled: false
```

> 关于配置可参考源码：`org.springframework.cloud.openfeign.FeignAutoConfiguration`

> 调用会进入`feign.hc5.ApacheHttp5Client`#execute

**配置OkHttp**

```xml
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-okhttp</artifactId>
</dependency>
```

```yaml
feign:
  okhttp:
    enabled: true
```

> 关于配置可参考源码： `org.springframework.cloud.openfeign.FeignAutoConfiguration`

> 调用会进入`feign.okhttp.OkHttpClient`#execute

**配置Gzip压缩**

```yaml
feign:
  compression:
    request:
      enabled: true
      mime-types: text/xml,application/xml,application/json
      # 最小请求压缩阈值
      min-request-size: 1024 
    response:
      enabled: true
```

> 当 Feign 的 HttpClient不是 okHttp的时候，压缩配置不会生效

> 配置源码在 `FeignAcceptGzipEncodingAutoConfiguration`

### 拦截器

通过拦截器实现参数传递, 扩展点:`feign.RequestInterceptor` 常见应用场景:

+ 链路追踪
+ 权限认证
+ 分布式事务
+ 等

**定义拦截器实现传递Trace-Id:**

```java
public class FeignAuthRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(null != attributes){
            HttpServletRequest request = attributes.getRequest();
            String traceId = request.getHeader("Trace-Id");
            template.header("Trace-Id",traceId);
        }
    }
}
```

**全局配置拦截器 - 方式一:**

```java
@Configuration
public class FeignConfiguration {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignAuthRequestInterceptor();
    }
}
```

**全局配置拦截器 - 方式二:**

```yaml
feign:
  client:
    config:
      # 所有服务生效
      default: 
        request-interceptors:
          - com.maple.user.interceptor.FeignAuthRequestInterceptor
```

**单独对某个服务配置拦截器:**

```yaml
feign:
  client:
    config:
      # 针对单个微服务进行配置
      other-micro: 
        request-interceptors:
          - com.maple.user.interceptor.FeignAuthRequestInterceptor
```

## 配置中心


#### 依赖 pom.xml
```xml
<!--   nacos 配置中心     -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<!--  新版本cloud已移除,配合nacos-config使用  -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```

#### bootstrap.yml 须新建
```yaml
spring:
  application:
    # 会自动的根据服务名拉去对应dataId名的配置文件
    name: spring-cloud-alibaba-user
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      username: nacos
      password: nacos
      config:
        namespace: adb6f806-e08a-41c0-9160-f63d6ac6a732 # 命名空间, 用于区分环境
        file-extension: yaml # 默认为 Properties
        shared-configs:
          - data-id: com.maple.material.common.yaml  # 配置文件名
            refresh: true # 实时更新
        group: material # 组名称, 一般用于区分项目
```

#### 通过 nacos dashboard 创建 data-id

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230707182403844.png)

### 使用 (@RefreshScope / @Value)
```java
@RefreshScope
@RequestMapping("/nacos")
@RestController
public class NacosConfigController {

    @Value("${user.name}")
    String name;

    @GetMapping("/config")
    public Result demo(){
        return Result.success(this.name);
    }
}
```