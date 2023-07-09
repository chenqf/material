# Nacos

## docker环境下nacos集群部署及nacos数据持久化

TODO

## 注册中心

#### 依赖 pom.xml

```xml
<!--   nacos 服务注册发现     -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<!--  nacos 不再自带Ribbon,须单独引用springCloudLoadbalancer -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

#### 配置 application.yml
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

#### 使用RestTemplate

```java
@Configuration
public class RestTemplateConfiguration {

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
    RestTemplate restTemplate;

    @GetMapping("/stock")
    public Result demo(){
        // spring-cloud-alibaba-stock 为其他微服务在nacos中注册的应用名
        Result<Integer> r = this.restTemplate.getForObject("http://spring-cloud-alibaba-stock/stock/num", Result.class);
        return Result.success("user:chenqf;stock:" + r.getData());
    }
}
```

## nacos注册中心结合OpenFeign

#### 依赖 pom.xml

```xml
<!--  openFeign  -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```
#### 使用

```java
@SpringBootApplication
@EnableFeignClients // 开启Feign
public class UserApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(UserApplication.class, args);
    }
}
```
```java
@FeignClient(name = "spring-cloud-alibaba-stock",path = "/stock")
public interface StockFeignService { // 该接口无需实现, 但方法返回值类型,注解,方法名要和被调用的服务controller一致
    @GetMapping("/num")
    Result<Integer> stock();
}
```
```java
@RequestMapping("/feign")
@RestController
public class FeignController {

    @Autowired
    StockFeignService stockFeignService;

    @GetMapping("/stock")
    public Result demo(){
        Result<Integer> r = this.stockFeignService.stock();
        return Result.success("user:chenqf;stock:" + r.getData());
    }
}
```
### Feign 配置
```yaml
logging:
  level:
    com.maple.user.feign: debug # 指定该路径下的日志界别为debug----本地开发时配置
feign:
  client:
    config:
      spring-cloud-alibaba-user: # 被调用服务在nacos中注册的应用名
        # 日志级别 
        logger-level: FULL
        # 连接超时时间 默认 2s
        connect-timeout: 5000
        # 请求处理超时时间 默认5s
        read-timeout: 10000
```

Feign日志级别:
+ NONE - 性能最佳, 不记录任何日志
+ BASIC - 适用于生产追踪问题 , 仅记录请求方法/url/状态码/执行时间
+ HEADERS - 记录请求和响应的header
+ FULL - 适用于开发测试环境, 记录请求和响应的header/body/元数据

### Feign 拦截器

> 主要用于隐式传递信息

```java
@Configuration
public class FeignConfiguration {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor(){
            @Override
            public void apply(RequestTemplate requestTemplate) {
                requestTemplate.header("name","value");
                requestTemplate.query("id","11");
                logger.info("feign 拦截器!");
            }
        };
    }
}
```

## 配置中心
![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230707214600578.png)

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