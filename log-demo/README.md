# 分布式日志处理

spring-boot 默认整合了logback用作日志输出

![image-20230831105008171](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230831105008171.png)

## Trace ID

微服务下, 须跟踪一个请求在不同应用中的日志, 需要为一个请求赋予`Trace ID`

一般情况下, `Trace ID`都是由网关层生成并存放在`header`中再发送给具体的应用

在`Filter`中获取`Trace ID`, 并将`Trace ID`添加至logback中

```java
@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TraceIdFilter());
        registrationBean.addUrlPatterns("/*"); // 设置过滤器拦截的URL模式
        return registrationBean;
    }
}
```

```java
public class TraceIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String traceId = httpRequest.getHeader("Trace-Id");
        if("".equals(traceId) || null == traceId){
            traceId = generateTraceId();
        }
        MDC.put("traceId", traceId);
        chain.doFilter(request, response);
        MDC.remove("traceId");
    }
    // 生成Trace ID的方法
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
```

## logback

### 开发时配置:

application.yaml文件中进行配置, 将日志级别设置为debug, 控制台中打印

```yaml
logging:
  level:
    root: debug # 全局日志级别
    com.maple: info # 具体包下的类的日志级别
```

### 生产环境配置 

使用`logback-spring.xml`进行配置, 按时间大小生成文件, 并指定最多保留日志份数

将所有INFO以上级别日志输出到一个日志文件中, 将ERROR以上级别日志输出到一个文件中

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
scan: 当配置文件被修改后, 将会被重新载入
scanPeriod: 见识配置文件是否有修改的间隔时间, 若没给出时间代为, 默认为毫秒
debug: 当此属性设置为true时, 将打印出logback内部日志信息, 默认为false
-->
<configuration scan="true" scanPeriod="60 seconds" debug="false">
    <!-- 全局配置，获取应用名称 -->
    <springProperty scope="context" name="SPRING_APP_NAME" source="spring.application.name"/>
    <property name="APP_NAME" value="${SPRING_APP_NAME}"/>
    <!-- 日志输出格式: %d表示日期时间, %thread表示线程名, %logger:类名, %-5level:级别从左显示5个字符, %msg:日志消息, %n:换行 -->
    <property name="FILE_LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [${SPRING_APP_NAME:-unknown}] [%thread] %logger{36} - traceId:%X{traceId} - %msg%n" />

    <!-- 输出到控制台 -->
    <appender name="consoleAppender" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>utf-8</charset>
        </encoder>
    </appender>

    <!--  所有日志输出到文件 - Production  -->
    <appender name="fileAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 基于时间和大小的滚动策略 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <!-- 日志文件输出的文件名, 必须包含%i, 从0开始 -->
            <fileNamePattern>logs/${APP_NAME:-app}.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <!-- 设置最大保留的历史日志文件数量 -->
            <maxHistory>30</maxHistory>
            <!-- 每个文件最大20MB,超过最大值新建一个文件 -->
            <maxFileSize>20MB</maxFileSize>
            <!-- 所有日志加起来的最大的大小 -->
            <totalSizeCap>400MB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>utf-8</charset>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>info</level>
        </filter>
    </appender>

    <!--  Error日志单独输入到一个文件  -->
    <appender name="errorFileAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/error/${SPRING_APP_NAME:-app}.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
        </rollingPolicy>
        <encoder>
            <pattern>${FILE_LOG_PATTERN}</pattern>
            <charset>utf-8</charset>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>error</level>
        </filter>
    </appender>

    <!-- 异步输出 -->
    <appender name="asyncAppender" class="ch.qos.logback.classic.AsyncAppender">
        <!-- 默认如果队列的80%已满,则会丢弃TRACT、DEBUG、INFO级别的日志，若要保留全部日志，设置为0 -->
        <discardingThreshold>0</discardingThreshold>
        <!-- 更改默认的队列的深度,该值会影响性能.默认值为256 -->
        <queueSize>256</queueSize>
        <!-- 添加附加的appender,最多只能添加一个 -->
        <appender-ref ref="fileAppender"/>
        <includeCallerData>true</includeCallerData>
    </appender>

    <!--  必选节点, 指定日志的输出级别, 只有一个level属性  -->
    <!--  生产环境INFO  -->
    <root level="INFO">
        <!--  开启控制台输出  -->
        <appender-ref ref="consoleAppender"/>
        <!--  开启文件输出  -->
        <appender-ref ref="asyncAppender"/>
        <!-- error 日志 -->
        <appender-ref ref="errorFileAppender"/>
    </root>
</configuration>
```

服务器环境部署时, 可通过多个logback.xml进行区分环境, 配置文件存放服务器中

+ logback-dev.xml：用于开发环境
+ logback-test.xml：用于测试环境
+ logback-prod.xml：用于生产环境

在启动时指定当前环境所需的配置文件:

```shell
java -jar /path/xx.jar --logging.config=/path/logback-spring-prod.xml
```

> 构建应用时删除application.yaml, 不使用spring.profiles.active进行多环境配置, 将配置全部外置到K8S的configmap中

### 日志四要素

1. 时间(年月日时分秒毫秒)
2. 地点(那个服务器哪个文件)
3. 人物(traceId/deviceId/token/userId)
4. 事件(做了什么事)

## Filebeat

集群环境, 多台机器, 每台机器安装一个Filebeat用于收集日志

+ 专门收集日志, 资源占用少
+ 可发送至ES/logstash/Kafka等
+ 异常中断重启后会继续上次停止位置(${filebeat_home}/data/registry文件记录日志偏移量)
+ 背压协议, 在logstash忙的时候, 减慢读取传输速度

下载并解压:

```shell
wget https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-7.17.3-linux-x86_64.tar.gz
tar xvf filebeat-7.17.3-linux-x86_64.tar.gz
mv filebeat-7.17.3-linux-x86_64 filebeat
```

```shell
# 启动filebeat
./filebeat -c filebeat_conf.yml # -e 查看filebeat日志 
# 后台启动, 使用exit才不会退出程序
nohup ./filebeat -c filebeat_conf.yml >/dev/null 2>&1 &
```

## Logstash

+ 可用来收集日志,一般不用, 使用Filebeat
+ 是一个数据管道, 用于对数据进行清洗过滤

下载并解压:

```shell
wget https://artifacts.elastic.co/downloads/logstash/logstash-7.17.3-linux-x86_64.tar.gz
tar xvf logstash-7.17.3-linux-x86_64.tar.gz
mv logstash-7.17.3-linux-x86_64 logstash
```

启动:
```shell
bin/logstash -f logstash.conf
# 后台启动
nohup bin/logstash -f logstash.conf >/dev/null 2>&1 &
```

配置文件包含三部分:
+ input : 数据来源
+ filter : 过滤清洗
+ output : 数据输出到哪

## ELK

+ Filebeat -> ElasticSearch
+ Filebeat -> Logstash -> ElasticSearch
+ Filebeat -> Kafka -> Logstash -> ElasticSearch

### Filebeat -> ElasticSearch

收集到的日志, 不做任何数据清洗, 发送到ElasticSearch

```yaml
filebeat.inputs:
- type: log
  enabled: true
  tags: ["micr_all"]
  paths:
    - /software/test_jar/logs/*.log # 收集日志的位置
  multiline:
    pattern: '^\[[0-9]{4}-[0-9]{2}-[0-9]{2}' # 处理error日志, 将非日期开头的信息算作前一行
    negate: true
    match: after
- type: log
  enabled: true
  tags: ["micr_error"]
  paths:
    - /software/test_jar/logs/error/*.log # 收集日志的位置
  multiline:
    pattern: '^\[[0-9]{4}-[0-9]{2}-[0-9]{2}' # 处理error日志, 将非日期开头的信息算作前一行
    negate: true
    match: after
output.elasticsearch:
  hosts: ["localhost:9200"] # elasticsearch的集群地址
  indices:
    - index: "app-all-log-%{+yyyy.MM.dd}" # 所有日志对应的ES索引
      when.contains:
        tags: "micr_all"
    - index: "app-error-log-%{+yyyy.MM.dd}" # Error日志对应的ES索引
      when.contains:
        tags: "micr_error"
  #username: "elastic"
  #password: "changeme"
```

### Filebeat -> Logstash -> ElasticSearch

Filebeat收集日志发送给Logstash, Logstash进行清洗过滤数据最后发送给ElasticSearch

**Filebeat配置:**

```yaml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /software/test_jar/logs/*.log # 收集日志的位置
    fields:
      log_type: all # 指定当前为所有日志
    multiline:
      pattern: '^\[[0-9]{4}-[0-9]{2}-[0-9]{2}' # 处理error日志, 将非日期开头的信息算作前一行
      negate: true
      match: after
  - type: log
    enabled: true
    paths:
      - /software/test_jar/logs/error/*.log # 收集日志的位置
    fields:
      log_type: error # 指定当前仅为ERROR日志
    multiline:
      pattern: '^\[[0-9]{4}-[0-9]{2}-[0-9]{2}' # 处理error日志, 将非日期开头的信息算作前一行
      negate: true
      match: after
output.logstash:
  hosts: ["localhost:5044"] # logstash对应的位置, 可指定多个
  loadbalance: true # 随机找一个logstash进行传输, 若失败换下一个
  index: filebeat
```

**Logstash配置:**

通过filebeat中的fields.log_type判断当前日志来源, 分别发往不同Index

```shell
input {
  beats {
    port => 5044
    codec=>plain{
      charset=>"UTF-8"
    }
  }
}
filter {
  if [fields][log_type] == "all" {
    mutate { add_field => { "[@metadata][index_prefix]" => "app-all-log" } }
  } else if [fields][log_type] == "error" {
    mutate { add_field => { "[@metadata][index_prefix]" => "app-error-log" } }
  }
  grok {
    overwrite => ["message"]
    match => {"message" => "(?m)^\[%{TIMESTAMP_ISO8601:timestamp}\] %{LOGLEVEL:level}\s*\[%{DATA:appName}\]\s*\[%{DATA:thread}\]\s*%{DATA:class} - traceId:%{DATA:traceId} - (?<message>.*)"}
  }
  grok {
    overwrite => ["message"]
    match => {"message" => "(?m)(?<message>.*?)\n(?<stacktrace>.*)"}
  }
  date {
    match => ["timestamp","yyyy-MM-dd HH:mm:ss.SSS"]
    target => "indexTime" # 将解析后的日期保存到这个字段中
  }
  ruby {
    code => "event.set('index.date', event.get('indexTime').time.localtime.strftime('%Y.%m.%d'))" # 基于indexTime字段获取日期放入index.date
  }
  mutate {
    remove_field => ["indexTime"]
  }
  prune { 
    whitelist_names => ["index.date","timestamp","level","appName","thread","class","traceId","message","stacktrace"] # 仅保留如下字段
  }
}
output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "%{[@metadata][index_prefix]}-%{index.date}" 
  }
  stdout{
    codec=>rubydebug
  }
}
```

获取字段如下:

+ appName : 应用名称
+ timestamp : 打印日志的时间
+ level : 日志级别
+ thread : 所属线程名
+ class : 所属类
+ traceId : 跟踪标识
+ message : 日志消息
+ stacktrace : error级别日志的堆栈信息

### Filebeat -> Kafka -> Logstash -> ElasticSearch

Filebeat收集数据发送给Kafka, Kafka传送数据给Logstash, Logstash进行清洗过滤数据最后发送给ElasticSearch

Logstash是基于内存的, 容易出现宕机, Kafka保证了日志不会丢失

**Filebeat配置:**

```yaml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /software/test_jar/logs/*.log # 收集日志的位置
    fields:
      log_topic: micr_all_log
      log_type: all 
    multiline:
      pattern: '^\[[0-9]{4}-[0-9]{2}-[0-9]{2}' # 处理error日志, 将非日期开头的信息算作前一行
      negate: true
      match: after
  - type: log
    enabled: true
    paths:
      - /software/test_jar/logs/error/*.log # 收集日志的位置
    fields:
      log_topic: micr_error_log
      log_type: error 
    multiline:
      pattern: '^\[[0-9]{4}-[0-9]{2}-[0-9]{2}' # 处理error日志, 将非日期开头的信息算作前一行
      negate: true
      match: after
output.kafka:
  hosts: ["localhost:9092"] #Kafka集群地址
  topic: '%{[fields.log_topic]}' # 主题名(自动创建)
  partition.round_robin:
    reachable_only: false
  required_acks: 1
  compression: gzip
  max_message_bytes: 1000000 # 单位B
```

**Logstash配置:**

```shell
input {
  kafka {
    bootstrap_servers => ["localhost:9092"]
    topics => ["micr_all_log","micr_error_log"]
    group_id => "logstash"
    auto_offset_reset => "latest"
    consumer_threads => 1
    codec => json
  }
}
filter {
  if [fields][log_type] == "all" {
    mutate { add_field => { "[@metadata][index_prefix]" => "app-all-log" } }
  } else if [fields][log_type] == "error" {
    mutate { add_field => { "[@metadata][index_prefix]" => "app-error-log" } }
  }
  grok {
    overwrite => ["message"]
    match => {"message" => "(?m)^\[%{TIMESTAMP_ISO8601:timestamp}\] %{LOGLEVEL:level}\s*\[%{DATA:appName}\]\s*\[%{DATA:thread}\]\s*%{DATA:class} - traceId:%{DATA:traceId} - (?<message>.*)"}
  }
  grok {
    overwrite => ["message"]
    match => {"message" => "(?m)(?<message>.*?)\n(?<stacktrace>.*)"}
  }
  date {
    match => ["timestamp","yyyy-MM-dd HH:mm:ss.SSS"]
    target => "indexTime" # 将解析后的日期保存到这个字段中
  }
  ruby {
    code => "event.set('index.date', event.get('indexTime').time.localtime.strftime('%Y.%m.%d'))" # 基于indexTime字段获取日期放入index.date
  }
  mutate {
    remove_field => ["indexTime"]
  }
  prune { 
    whitelist_names => ["index.date","timestamp","level","appName","thread","class","traceId","message","stacktrace"] # 仅保留如下字段
  }
}
output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "%{[@metadata][index_prefix]}-%{index.date}" 
  }
  stdout{
    codec=>rubydebug
  }
}
```

## Kibana 查看日志

## TODO

云原生 下使用 loki 收集日志 promtail

![image-20230901094259663](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230901094259663.png)

![image-20230901113916443](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230901113916443.png)
