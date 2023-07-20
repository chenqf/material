# RabbitMQ

可靠性高, 功能全面, 消息基本不会丢, 吞吐量比较低(2-3W/s), 消息积累影响性能

> 提升系统异步通信、扩展解耦能力

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710095031737.png)

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710095301511.png)

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710095323886.png)

1. 队列(queue) : 点对点消息通信 (point to point)
   + 消息只有唯一的发送者和接受者, 但并不是说只能有一个接受者
2. 主题(topic) : 发布(publish) / 订阅(subscribe) 消息通信
   + 发送者发送消息到topic, 多个接收者监听topic, 消息到达topic同时收到消息

## 队列   

+ Classic - 高可用结合镜像队列 (3.8前推荐使用)
+ Quorum - 3.8+ 官方推荐
+ Stream - 3.9+新增, 太新(不是很完善), 暂不使用

### Classic

> 须设置 durable=true 设置消息硬盘持久化

### Quorum 仲裁队列

集群中推荐使用, 集群中节点多数(N/2+1)认为写成功, 最终认为写成功

#### Poison Message handling 有毒消息处理

消息消费过程中,出现异常, 多次重新入队, 此类消息称之为 Poison Message

消息投递次数, 记录在 x-delivery-count 头信息中

可设置 Delivery limit 参数设定有毒消息的删除策略, 若配置了死信队列, 就进入死信队列

#### Stream

## 简单使用

#### pom.xml
```xml
<dependency>
   <groupId>org.springframework.amqp</groupId>
   <artifactId>spring-rabbit</artifactId>
</dependency>
```
#### application.yml
```yaml
spring:
   application:
      name: rabbitMQ-demo
   rabbitmq:
      host: ${CLOUD_IP}
      port: 5672
      username: admin
      password: chenqf
      virtual-host: /
```

#### 开启Rabbit使用
```java
@EnableRabbit
@SpringBootApplication
public class RabbitMQApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(RabbitMQApplication.class, args);
    }
}
```
#### 序列化方式
```java
@Configuration
public class AmqpConfig {
    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
```

#### 服务启动自动创建 exchange / queue / binding 

```java
@Configuration
public class AmqpConfig {
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Bean
    public Queue demoQueue() {
        Queue queue = new Queue("mirror_demo");
        amqpAdmin.declareQueue(queue);
        return queue;
    }
    @Bean
    public DirectExchange demoExchange() {
        DirectExchange exchange = new DirectExchange("demo");
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(demoQueue()).to(demoExchange()).with("demoKey");
    }
}
```

#### 发送消息
```java
@RestController
@RequestMapping("/send")
public class MqSentController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/demo")
    public String direct1(){
        rabbitTemplate.convertAndSend("demo","demoKey",new Book(1,"chenqf","haha"));
        return "";
    }
}
```
#### 接收消息
```java
@Service
public class BookService {
    @RabbitListener(queues = "mirror_demo")
    public void receive(Book book){ // 监听消息
        System.out.println("收到消息:" + book);
    }
}
```

## 避免避免消息丢失

### 消费者手动应答

> 方式一 - 全局配置

```yaml
spring:
  application:
    name: rabbitMQ-demo
  rabbitmq:
    host: ${CLOUD.IP}
    port: 5672
    username: admin
    password: chenqf
    virtual-host: /
    listener:
      simple:
        acknowledge-mode: manual
      direct:
        acknowledge-mode: manual
```

> 方式二 - 单独配置

```java
@Service
public class BookService {

    @Autowired
    private ObjectMapper objectMapper;


    @RabbitListener(queues = "demo", ackMode = "MANUAL")
    public void receiveAck(Book book, Message message, Channel channel) throws IOException {
        try {
            int i = 1 / 0;
            System.out.println("收到消息:" + book);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```



+ RabbitMq分发日志
+ RabbitMq动态监听
+ RabbitMq死信队列
+ RabbitMq延时队列
+ RabbitMq消息重试
+ 如何保证分布式事务最终一致性
  + 事务消息机制
  + 幂等消费
+ MQTT协议


https://blog.csdn.net/qq_26112725/article/details/129218163



## Rabbit Plugin

## Production 最佳实践