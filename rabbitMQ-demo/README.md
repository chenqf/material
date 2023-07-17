# RabbitMQ

> 提升系统异步通信、扩展解耦能力

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710095031737.png)

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710095301511.png)

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710095323886.png)

1. 队列(queue) : 点对点消息通信 (point to point)
   + 消息只有唯一的发送者和接受者, 但并不是说只能有一个接受者
2. 主题(topic) : 发布(publish) / 订阅(subscribe) 消息通信
   + 发送者发送消息到topic, 多个接收者监听topic, 消息到达topic同时收到消息
   
> JMS (Java Message Service) java 消息服务 (基于JVM消息代理的规范)
> + ActiveMQ
> + HornetMQ
> 
> AMQP (Advanced Message Queuing Protocol) 兼容 JMS
> + RabbitMQ


## docker 部署 rabbitMQ
```shell
# 5672 消息队列协议端口
# 15672 Web管理界面端口
# 61613 消息文本协议
# 1883 MQTT协议
# RABBITMQ_DEFAULT_USER 用户名
# RABBITMQ_DEFAULT_PASS 密码
export USERNAME=admin
export PASSWORD=chenqf
export VERSION=3.12.1
docker run -d --name rabbitmq -e RABBITMQ_DEFAULT_USER=${USERNAME} -e RABBITMQ_DEFAULT_PASS=${PASSWORD} -p 15672:15672 -p 5672:5672 -p 25672:25672 -p 61613:61613 -p 1883:1883 rabbitmq:${VERSION}-management
# 访问web界面: http://127.0.0.1:15672/
```

## docker 部署 rabbitMQ 集群

TODO

## K8S 部署 rabbitMQ

TODO

## 使用

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710105604527.png)

#### 在rabbitMQ中增加Exchange

![](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710105229243.png)

#### 在rabbitMQ中增加Queue

![image-20230710105857886](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710105857886.png)

#### 给exchange绑定queue

![image-20230710110608008](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710110608008.png)

![image-20230710110541150](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710110541150.png)

![image-20230710110726117](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230710110726117.png)

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
#### 发送消息
```java
@RestController
@RequestMapping("/send")
public class MqSentController {

   @Autowired
   private RabbitTemplate rabbitTemplate;

   @RequestMapping("/direct1")
   public Result direct1(){
      rabbitTemplate.convertAndSend("exchanges.direct","China.people","来自springBoot的message");
      return Result.success("");
   }

   @RequestMapping("/direct2")
   public Result direct2(){
      Book book = new Book(1,"好好学习","chenqf");
      rabbitTemplate.convertAndSend("exchanges.direct","China",book);
      return Result.success("");
   }

   @RequestMapping("/fanout1")
   public Result fanout1(){
      rabbitTemplate.convertAndSend("exchanges.fanout","","来自springBoot的 fanout message");
      return Result.success("");
   }

   @RequestMapping("/topic1")
   public Result topic1(){
      rabbitTemplate.convertAndSend("exchanges.topic","haha.news","来自springBoot的 topic message");
      return Result.success("");
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

#### 获取数据
```java
@RestController
@RequestMapping("/receive")
public class MqReceiveController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/China")
    public Result China(){
        Object o = rabbitTemplate.receiveAndConvert("China");
        System.out.println(o.getClass());
        System.out.println(o.toString());
        return Result.success(o.toString());
    }
}
```

#### 监听消息

> 集群分布式环境下, 只有一台服务会收到消息

```java
@Service
public class BookService {
    @RabbitListener(queues = "China")
    public void receive(Book book){ // 监听消息
        System.out.println("收到消息:" + book);
    }

    @RabbitListener(queues = "China.news")
    public void receive(Message message){ // 监听头信息
        System.out.println(message.getBody().toString());
        System.out.println(message.getMessageProperties().toString());
    }
}
```

#### AmqpAdmin 管理组件

> 通过代码维护 exchange / queue / binding

```java
@RestController
@RequestMapping("/amqp")
public class AmqpAdminController {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @RequestMapping("/create")
    public Result create(){

        // 创建 exchange
        amqpAdmin.declareExchange(new DirectExchange("exchanges.direct1"));
        amqpAdmin.declareExchange(new FanoutExchange("exchanges.fanout1"));
        amqpAdmin.declareExchange(new TopicExchange("exchanges.topic1"));

        // 创建 Queue
        amqpAdmin.declareQueue(new Queue("amqp.queue",true));

        // 创建 Binding
        amqpAdmin.declareBinding(new Binding("amqp.queue", Binding.DestinationType.QUEUE,"exchanges.direct1","routingKey-a",null));

        return Result.success("");
    }

    @RequestMapping("/delete")
    public Result delete(){

        // 删除 exchange
        amqpAdmin.deleteExchange("exchanges.direct1");
        amqpAdmin.deleteExchange("exchanges.fanout1");
        amqpAdmin.deleteExchange("exchanges.topic1");

        // 删除 queue
        amqpAdmin.deleteQueue("amqp.queue");

        return Result.success("");
    }
}
```