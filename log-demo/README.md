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
            <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] %-5level [%thread] %logger{36} - traceId:%X{traceId} - %msg%n</pattern>
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
./filebeat -c filebeat.yml & # -e 查看filebeat日志 
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
bin/logstash -f job/filebeat.conf
```

配置文件包含三部分:
+ input : 数据来源
+ filter : 过滤清洗
+ output : 数据输出到哪

## ELK

+ Filebeat -> ElasticSearch
+ Filebeat -> Logstash -> ElasticSearch
+ Filebeat -> Kafka -> Logstash -> ElasticSearch

## Filebeat -> ElasticSearch

收集到的日志, 不做任何数据清洗, 发送到ElasticSearch

```yaml
filebeat.inputs:
- type: log
  enabled: true
  tags: ["micr"]
  paths:
    - /software/test_jar/logs/*.log # 收集日志的位置
  multiline:
    pattern: '^\[[0-9]{4}-[0-9]{2}-[0-9]{2}' # 处理error日志, 将非日期开头的信息算作前一行
    negate: true
    match: after
output.elasticsearch:
  hosts: ["localhost:9200"] # elasticsearch的集群地址
  indices:
    - index: "myapp-log-from-filebeat-%{+yyyy.MM.dd}" # 发送到elasticsearch的indexName(自动创建)
      when.contains:
        tags: "micr"
  #username: "elastic"
  #password: "changeme"
```

## Filebeat -> Logstash -> ElasticSearch

Filebeat配置:

```yaml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /software/test_jar/logs/*.log # 收集日志的位置
  multiline:
    pattern: '^\[[0-9]{4}-[0-9]{2}-[0-9]{2}' # 处理error日志, 将非日期开头的信息算作前一行
    negate: true
    match: after
output.logstash:
  hosts: ["localhost:5044"] # logstash对应的位置
```

Logstash配置:

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
  grok {
    overwrite => ["message"]
    match => {"message" => "(?m)^\[%{TIMESTAMP_ISO8601:timestamp}\] %{LOGLEVEL:level}\s*\[%{DATA:thread}\]\s*%{DATA:class} - traceId:%{DATA:traceId} - (?<message>.*)"}
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
    code => "event.set('index.date', event.get('indexTime').time.localtime.strftime('%Y-%m-%d'))" # 基于indexTime字段获取日期放入index.date
  }
  mutate {
    remove_field => ["indexTime"]
  }
}
output {
  elasticsearch {
    hosts => ["localhost:9200"]
    #index => "myapp-log-from-filebeat-logstash-%{+YYYY-MM-dd}" # 以当前系统时间作为index
    index => "myapp-log-from-filebeat-logstash-%{index.date}" # 以日志中时间作为index

  }
  stdout{
    codec=>rubydebug
  }
}
```

获取字段如下:

+ timestamp : 打印日志的时间
+ level : 日志级别
+ thread : 所属线程名
+ class : 所属类
+ traceId : 跟踪标识
+ message : 日志消息
+ stacktrace : error级别日志的堆栈信息

  
**发送至Kafka**

```yaml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /software/test_jar/logs/*.log # 收集日志的位置
filebeat.config.modules:
  path: ${path.config}/modules.d/*.yml
  reload.enabled: false
setup.template.settings:
  index.number_of_shards: 1
output.kafka:
  hosts: ["localhost:9092"] #集群地址
  topic: '%{[fields.log_topic]}' # 主题名(自动创建)
  partition.round_robin:
    reachable_only: false
  required_acks: 1
  compression: gzip
  max_message_bytes: 1000000 # 单位B
processors:
  - add_host_metadata:
      when.not.contains.tags: forwarded
  - add_cloud_metadata: ~
  - add_docker_metadata: ~
  - add_kubernetes_metadata: ~
```



**从Filebeat接收数据**
```shell
input {
  beats {
    port=>5044
    codec=>plain{
      charset=>"UTF-8"
    }
  }
}
```

**从Kafka接收数据**
```shell
input {
  kafka {
    bootstrap_servers => ["localhost:9092"]
    topics => ["myapp"]
    group_id => "myapp_log"
    consumer_threads => 1
    codec => json
    type => myapp
  }
}
```

**对kafka中传输的数据json后进行匹配**
```shell
filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:loglevel}  \[%{DATA:thread}\] %{DATA:class} - traceId:%{DATA:traceId} - %{GREEDYDATA:message}" }
  }
}
```


logstash解析数据传输致ES时, 要使用日志中的时间作为索引而不是当前时间作为索引



Filebeat部署

Logstash部署

ES部署

Logstash 集群?

promtail 收集日志 loki 云原生