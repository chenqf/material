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

#### Classic

消息消费后, 必定删除

#### Quorum

#### Stream


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

#### 使用
```java
@EnableRabbit
@SpringBootApplication
public class RabbitMQApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(RabbitMQApplication.class, args);
    }
}
```

> 当消息为对象时, 使用的是jvm的序列化, 修改为JSON格式

```java
@Configuration
public class AmqpConfig {
    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
```

> 设置服务启动时自动创建exchange/queues/binding(若存在则服用)

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

> 发送消息
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
> 接收消息
```java
@Service
public class BookService {
    @RabbitListener(queues = "mirror_demo")
    public void receive(Book book){ // 监听消息
        System.out.println("收到消息:" + book);
    }
}
```


+ RabbitMq分发日志
+ RabbitMq动态监听
+ RabbitMq持久化
+ RabbitMq死信队列
+ RabbitMq延时队列
+ RabbitMq消息重试
+ 如何保证分布式事务最终一致性
  + 事务消息机制
  + 幂等消费
+ MQTT协议