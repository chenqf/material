# Swagger

> 开启Swagger影响性能, production环境一定不能启用

```xml
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-boot-starter</artifactId>
    <version>${swagger.version}</version>
</dependency>
```
```yaml
server:
  port: 8007
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
  application:
    name: swagger-demo
swagger:
  enable: true # 是否启用swagger, production使用false不启用
```
```java
@EnableOpenApi // 开启Swagger3
@SpringBootApplication
public class SwaggerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(SwaggerApplication.class, args);
    }
}
```

```java
@Configuration
public class SpringFoxSwaggerConfig {

  @Value("${swagger.enable}")
  private boolean enable;

  @Bean
  public Docket docket() {
    return new Docket(DocumentationType.OAS_30) // 指定3.0版本
            .groupName("开发组1") // 定义多个Docker,可区分多个开发组
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.maple.swagger.controller")) // 指定接口扫描的为位置
            .paths(PathSelectors.ant("/**")) // 执行接口扫描的路径
            .build()
            .apiInfo(apiInfo())
            .enable(enable); // 开启swagger影响性能, production环境一定不能开启
  }

  @Bean
  public ApiInfo apiInfo() {
    return new ApiInfoBuilder()
            .title("Swagger Test App Restful API")
            .description("swagger test app restful api")
            .contact(new Contact("chenqf","https://github.com/chenqf","546710115@qq.com"))
            .termsOfServiceUrl("https://github.com/chenqf")
            .version("1.0")
            .build();
  }

  @Bean
  public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
    return new BeanPostProcessor() {

      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
          customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
        }
        return bean;
      }

      private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
        List<T> copy = mappings.stream()
                .filter(mapping -> mapping.getPatternParser() == null)
                .collect(Collectors.toList());
        mappings.clear();
        mappings.addAll(copy);
      }

      @SuppressWarnings("unchecked")
      private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
        try {
          Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
          field.setAccessible(true);
          return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      }
    };
  }
}
```

> Swagger UI 访问地址: http://host:port/swagger_ui/index.html

## Swagger注解

+ @Api 用在Controller上, 表示对Controller的说明
+ @ApiOperation 用在method上, 表示method的用途
+ @ApiImplicitParams 用在method上, 表示参数说明
  + name: 参数名
  + value: 参数说明
  + required: 是否必填
  + paramType: 参数放在那个地方
    + header ---> @RequestHeader
    + query ----> @RequestParam
    + path -----> @PathVariable
  + dataType: 参数类型,默认String
  + defaultValue: 参数默认值

```java
@RequestMapping("/demo")
@RestController
@Api(value = "测试SwaggerController")
public class DemoController {

    @PostMapping("/test1")
    @ApiOperation(value = "测试方法1")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name",value = "姓名",required = true, paramType = "query"),
            @ApiImplicitParam(name = "age",value = "年龄",required = true, paramType = "query",dataType = "Integer")
    })
    public Result test1(String name, Integer age){
        return Result.success(name + "--" + age);
    }
}
```

+ @ApiModel 用于描述实体
+ @ApiModelProperty 用于描述实体参数

```java
@ApiModel("用户信息实体")
@Data
@AllArgsConstructor
public class User {
  @ApiModelProperty(value = "编号")
  private Integer id;
  @ApiModelProperty(value = "姓名",required = true)
  private String name;
  @ApiModelProperty(value = "年龄",required = true)
  private Integer age;
}
```

```java
@RequestMapping("/demo")
@RestController
@Api(value = "测试SwaggerController")
public class DemoController {

    @PostMapping("/addUser")
    @ApiOperation(value = "添加用户")
    public Result test2(User user) {
        return Result.success(user);
    }
}
```

## Docker

TODO

## Swagger Codegen
TODO

## Swagger Editor
TODO