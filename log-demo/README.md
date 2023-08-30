# 分布式日志处理

spring-boot 默认整合了logback用作日志输出

## logback

### 开发时配置:

application.yaml文件中进行配置, 将日志级别设置为debug, 控制台中打印

```yaml
logging:
  pattern:
    console: %d{yyyy-MM-dd HH:mm:ss.SSS} -- %-5level %logger{32} %thread -- %msg%n  # 控制台输出格式
  level:
    root: info # 全局日志级别
    com.maple: debug # 具体包下的类的日志级别
```

### 生产环境配置 

使用`logback-spring.xml`进行配置, 按时间大小生成文件, 并指定最多保留日志份数

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
scan: 当配置文件被修改后, 将会被重新载入
scanPeriod: 见识配置文件是否有修改的间隔时间, 若没给出时间代为, 默认为毫秒
debug: 当此属性设置为true时, 将打印出logback内部日志信息, 默认为false
-->
<configuration scan="true" scanPeriod="60 seconds" debug="false">
    <!--  输出到文件 - Production  -->
    <appender name="fileAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 基于时间和大小的滚动策略 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <!-- 日志文件输出的文件名, 必须包含%i, 从0开始 -->
            <fileNamePattern>logs/myapp.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <!-- 设置最大保留的历史日志文件数量 -->
            <maxHistory>30</maxHistory>
            <!-- 每个文件最大20MB,超过最大值新建一个文件 -->
            <maxFileSize>10MB</maxFileSize>
            <!-- 所有日志加起来的最大的大小 -->
            <totalSizeCap>400MB</totalSizeCap>
        </rollingPolicy>
        <!-- 日志输出到文件中的格式 -->
        <encoder>
            <!-- %d表示日期时间, %thread表示线程名, %logger:类名, %-5level:级别从左显示5个字符, %msg:日志消息, %n:换行 -->
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] -- %msg%n</pattern>
            <charset>utf-8</charset>
        </encoder>
    </appender>

    <!--  必选节点, 指定日志的输出级别, 只有一个level属性  -->
    <!--  生产环境INFO  -->
    <root level="INFO">
        <!--  开启文件输出  -->
        <appender-ref ref="fileAppender"/>
    </root>
</configuration>
```

服务器环境部署时, 可通过多个logback.xml进行区分环境, 配置文件存放服务器中

+ logback-dev.xml：用于开发环境
+ logback-test.xml：用于测试环境
+ logback-prod.xml：用于生产环境

在启动时指定当前环境所需的配置文件:

```shell
-Dlogging.config=/<path>/logback-spring-prod.xml
```

> 构建应用时删除application.yaml, 不使用spring.profiles.active进行多环境配置, 将配置全部外置到K8S的configmap中

### 日志四要素

1. 时间(年月日时分秒毫秒)
2. 地点(那个服务器哪个文件)
3. 人物(traceId/deviceId/token/userId)
4. 事件(做了什么事)





每个请求生成一个唯一表示 traceId, 日志打印的时候, 带上每个请求都使用一个唯一标识


听对话这位老哥对链路跟踪这块没什么概念，应该听说过Elastic APM。国内Java生态圈Skywalking应该占了半壁江山吧。微服务架构日志的记录应该是直接输出日志到控制台，通过filebeat或者promtail去收集，日志和链路这块最好能统一trace_id,方便定位与分析问题。日志这块除了ELK,EFK系列，Loki也挺好用的。再请教个问题，链路采样率，不知如何设置更合理。