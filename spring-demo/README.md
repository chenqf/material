# Spring & Spring Boot

## 容器

**启动容器过程中的事件监听:**

从`META-INF/spring.factories`中添加如下内容:

1. 以`org.springframework.boot.SpringApplicationRunListener`为Key
2. 以实现`SpringApplicationRunListener`的类路径为Value

![image-20230906200251524](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230906200251524.png)

```java
public class MySpringRunListener implements SpringApplicationRunListener{

    private SpringApplication application;

    private String[] args;

    public MySpringRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        System.out.println("开始启动");
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        System.out.println("环境准备完成:配置参数");
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        System.out.println("容器创建完成");
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        System.out.println("配置类注册完成");
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        System.out.println("容器启动完成,WEB服务器启动完成,ApplicationRunner和CommandLineRunner还未执行");
    }


    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
        System.out.println("容器准备好了,ApplicationRunner和CommandLineRunner执行完了");
    }


    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        System.out.println("容器创建失败");
    }
}
```

## Bean

### 创建bean

![image-20230905142944333](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230905142944333.png)

**生成普通对象**

![image-20230905115337118](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230905115337118.png)

**依赖注入**

![image-20230905115901718](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230905115901718.png)

**代理对象**

![image-20230905121653721](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230905121653721.png)

### bean声明周期

一个bean是如何生成的, 如何销毁的

TODO

## 自动配置

![image-20230906163924056](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230906163924056.png)

**@EnableAutoConfiguration -> @Import(AutoConfigurationImportSelector.class)**: 

1. 会到所有Jar包中寻找`spring.factories`文件
2. 根据`org.springframework.boot.autoconfigure.EnableAutoConfiguration`找到所有自动配置类
3. 返回给spring

**@AutoConfigurationPackage -> @Import(AutoConfigurationPackages.Registrar.class)**: 

1. 将扫描到的`root package path`注册为`BeanDefinition`
2. 用于给第三方自动配置jar包使用 (mybatis)

**@SpringBootConfiguration  -> @Configuration -> @Component:**

1. 自己本身也是一个配置类
2. 自己本社也是一个bean

**@ComponentScan:**

1. 扫描根路径下所有声明了@Controller/@Service/@Component的类作为bean

![image-20230905095835874](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230905095835874.png)

### 案例

现有一`common`项目, 希望在`common`中实现自动配置共通bean, 在其他子项目中引用`common`项目

**业务子项目引用common:**

```xml
<dependency>
    <groupId>com.maple</groupId>
    <artifactId>common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**common:**

![image-20230905103639021](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230905103639021.png)

```java
@Configuration
public class Spi1Config {
    @Bean
    public SpiDemo1 spiDemo1(){
        return new SpiDemo1(1l,"cqf");
    }
}
```

```java
@Configuration
public class Spi2Config {
    @Bean
    public SpiDemo2 spiDemo2(){
        return new SpiDemo2(2l,"chenqf");
    }
}
```

## 常用条件注解

**工作中用的不多, 主要看源码需要**

1. `@ConditionalOnBean` : 是否存在某个类或某个名字的Bean
2. `@ConditionalOnMissingBean` : 是否确实某个类或某个名字的Bean
3. `@ConditionalOnSingleCandidate` : 是否符合指定类型的Bean只有一个
   + 若找到多个, 但只有一个使用`@Primary`, 也认为只有一个
4. `@ConditionalOnClass` : 是否存在某个类
5. `@ConditionalOnMissingClass` : 是否缺失某个类
6. `@ConditionalOnExpression` : 指定的SPEL表达式返回的是true还是false
7. `@ConditionalOnJava` : 判断java版本
8. `@ConditionalOnWebApplication` : 当前是不是Web应用
9. `@ConditionalOnNotWebApplication` : 当前不是一个Web应用
10. `@ConditionalOnProperty` : Environment中是否存在某个属性

## 其他常用注解

#### **@Lazy**

告诉Spring容器在需要时才初始化Bean, 有助于提高应用程序的性能

#### **@Scope**

用于定义Bean的作用域，即Bean的生命周期范围

+ 单例（Singleton）
+ 原型（Prototype）
+ 会话（Session）
+ 请求（Request）等

#### **@Primary**

指定具有多个相同类型的Bean时，哪一个应该被优先选择

#### **@Qualifier**

当多个Bean都匹配某个类型时，可以使用`@Qualifier`注解指定要注入的具体Bean的名称

#### **@PropertySource**

用于加载外部属性文件中的属性值，并将其注入到Spring的环境中

## 参数配置

> 2.6+ properties 优先级大于 yaml

优先级从上到下排列:

1. 命令行参数( --k1=v1 --k2=v2 )
2. JVM参数( -Dk1=k2 )
3. 环境变量
4. file:/config/*/application.yaml
5. file:/config/application.yaml
6. file:/application.yaml
7. classpath:/config/application.yaml
8. classpath:/application.yaml

> bootstrap.yaml 是SpringCloud使用的, 和SpringBoot无关

## 过滤器

## 拦截器

## Controller切面

## 统一异常处理

