# RabbitMQ

可靠性高, 功能全面, 消息基本不会丢, 吞吐量比较低(2-3W/s), 消息积累影响性能

> 提升系统异步通信、扩展解耦能力

## 基本配置

```xml

<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit</artifactId>
</dependency>
```

```java

@EnableRabbit
@SpringBootApplication
public class RabbitMQApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(RabbitMQApplication.class, args);
    }
}   
```

```yaml
server:
  port: 8005
spring:
  application:
    name: rabbitMQ-demo
  rabbitmq:
    host: ${CLOUD.IP}
    port: 5672
    username: admin
    password: chenqf
    virtual-host: /
```

## 队列

+ Classic - 高可用结合镜像模式 (3.8前推荐使用)
+ Quorum - 3.8+ 官方推荐, 用于替代镜像模式
+ Stream - 3.9+新增, 太新(不是很完善), 暂不使用

### Classic 经典队列

经典队列, 须设置 durable=true 设置消息硬盘持久化

```java

@Configuration
public class AmqpConfig {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Bean
    public Queue classicQueue() {
        Queue queue = new Queue("classic-demo", true, false, false);
        amqpAdmin.declareQueue(queue);
        return queue;
    }
}
```

### Quorum 仲裁队列

官方推荐队列方式, 集群中推荐使用, 集群中节点多数(N/2+1)认为写成功, 最终认为写成功

消息消费过程中,出现异常, 多次重新入队, 此类消息称之为 Poison Message

消息投递次数, 记录在 x-delivery-count 头信息中

仲裁队列可设置 Delivery limit 参数设定有毒消息的删除策略, 若配置了死信队列, 就进入死信队列

```java

@Configuration
public class AmqpConfig {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Bean
    public Queue quorumQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-queue-type", "quorum"); // 指定为 quorum 类型的队列
        map.put("x-delivery-limit", 3); // 指定消息投递次数超过次数进入死信队列
        Queue queue = new Queue("quorum-demo", true, false, false, map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }
}
```

### 死信队列 & 延时队列

**死信队列:** 消息未进行正常消费的一种补救措施, 本质也是一个队列

**何时会产生死信**

+ 消息被消费者拒绝, 并且requeue=false
+ 消息达到过期时间(TTL), 且并未被消费
+ 队列达到最大长度限制而被丢弃
+ 仲裁队列消息重复投递次数超过DeliveryLimit

**延时队列:**

+ 利用普通队列的过期时间(TTL)
+ 超过TTL进入对应的死信队列
+ 客户端订阅对应的死信队列

```java

@Configuration
public class AmqpConfig {
    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     * 声明延时队列 ,声明过期时间, 声明对应的死信队列
     */
    @Bean
    public Queue delayQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", "dead-letter-exchange"); // 指定死信队列的exchange
        map.put("x-dead-letter-routing-key", "dead-routing-key"); // 指定死信队列的exchange
        map.put("x-message-ttl", 3000); // 过期时间, 单位ms, 超过时间进入死信队列
        Queue queue = new Queue("daily-demo", true, false, false, map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    /**
     * 声明用作死信的 quorum 队列
     */
    @Bean
    public Queue deadLetterQuorumQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-queue-type", "quorum"); // 指定为 quorum 类型的队列
        Queue queue = new Queue("dead-letter-quorum-demo", true, false, false, map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }

    /**
     * 绑定死信交换机和对应的队列
     */
    @Bean
    public Binding binding4() {
        return BindingBuilder.bind(deadLetterQuorumQueue()).to(deadLetterExchange()).with("dead-routing-key");
    }

}
```

### 惰性队列

3.6~3.11版本提供, 3.12+为队列的默认实现

为了解决消息积压的问题, 数百万级别。

惰性队列会尽可能早的将消息内容保存到硬盘当中，并且只有在用户请求到时，才临时从硬盘加载到RAM内存当中。

```java
@Configuration
public class AmqpConfig {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Bean
    public Queue classicQueue() {
        Map<String, Object> map = new HashMap<String, Object>();
        args.put("x-queue-mode", "lazy"); // 3.6~3.11 支持
        Queue queue = new Queue("classic-demo", true, false, false, map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }
}
```

## 消息确认 - 确保消息不丢

+ 发送方确认
    + Producer到Exchange的确认
    + Exchange到Queue的确认
+ 接收方确认
    + Consumer到Queue的确认

策略:

+ 发送消息异常时, 将消息`重新发送`, 限定一定次数, 超过限定次数, 发送至`报警队列`和`备用对列`
+ 接收消息异常时, 将消息`重新入队`, 限定一定次数, 超过限定次数, 发送至`报警队列`和`备用对列`

报警队列: 用于通知管理员

备用队列: 用于将消息保存, 方便人工处理

### 发送方确认

```yaml
spring:
  rabbitmq:
    publisher-confirm-type: correlated # 确认消息已发送到Exchange
    publisher-returns: true # 确认消息已发送到Queue
```

Producer到Exchange的确认回调在当前进程内触发, 且无法获取消息的内容, 故通过map维护对应关系(也可将对象关系存储在redis)

Exchange到Queue的确认回调可以获取到消息体及消息头,且存在分布式问题, 故通过消息头维护对应关系

```java

@Component
public class MessageSender {
    public static Map<String, Map<String, Object>> concurrentHashMap = new ConcurrentHashMap<>();
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void rabbitConvertAndSend(String exchange, String routingKey, Object message) {
        this.rabbitConvertAndSend(exchange, routingKey, message, null);

    }

    public void rabbitConvertAndSend(String exchange, String routingKey, Object data, CorrelationData correlationData) {
        String uuid = correlationData == null ? UUID.randomUUID().toString() : correlationData.getId();
        correlationData = correlationData == null ? new CorrelationData(uuid) : correlationData;
        if (concurrentHashMap.containsKey(uuid)) {
            Map<String, Object> map = concurrentHashMap.get(uuid);
            map.put("retry-count", (int) map.get("retry-count") + 1);
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("retry-count", 0);
            map.put("exchange", exchange);
            map.put("routingKey", routingKey);
            map.put("correlationData", correlationData);
            concurrentHashMap.put(uuid, map);
        }
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                concurrentHashMap.get(uuid).put("message", message);
                return message;
            }
        };
        this.rabbitTemplate.convertAndSend(exchange, routingKey, data, messagePostProcessor, correlationData);
    }
}
```

```java

@Configuration
public class RabbitConfig {
    // 失败时重复调用次数
    private final static int RETRY_COUNT = 5;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MessageSender messageSender;

    @PostConstruct
    public void init() {
        rabbitTemplate.setMandatory(true);
        // 确认消息送到交换机(Exchange)回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) {
                return;
            }
            if (correlationData.getId().isEmpty()) {
                return;
            }
            String msgId = correlationData.getId();
            if (ack) {
                MessageSender.concurrentHashMap.remove(msgId);
            } else if (MessageSender.concurrentHashMap.containsKey(msgId)) {
                Map<String, Object> map = MessageSender.concurrentHashMap.get(msgId);
                int retryCount = (int) map.get("retry-count");
                Message message = (Message) map.get("message");
                String exchange = (String) map.get("exchange");
                String routingKey = (String) map.get("routingKey");
                // 在重试次数内 - 重新发送
                if (retryCount < RETRY_COUNT) {
                    messageSender.rabbitConvertAndSend(exchange, routingKey, message, correlationData);
                } else {
                    message.getMessageProperties().setHeader("previous-exchange", exchange);
                    message.getMessageProperties().setHeader("previous-routingKey", routingKey);
                    message.getMessageProperties().setHeader("retry-count", retryCount + 1);
                    rabbitTemplate.convertAndSend("exchange-spare", "routingKey", message);
                }
            }
        });

        // exchange -> queue 错误回调
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            Message message = returnedMessage.getMessage();
            Object o = message.getMessageProperties().getHeaders().get("retry-count");
            long retryCount = o == null ? 0 : (long) o;
            // 在重试次数内 - 重新发送
            if (retryCount < RETRY_COUNT) {
                message.getMessageProperties().setHeader("retry-count", retryCount + 1);
                rabbitTemplate.convertAndSend(returnedMessage.getExchange(), returnedMessage.getRoutingKey(), returnedMessage.getMessage());
            }
            // 将原 exchange routingKey 通过 header 保存, 将消息发送到备用队列
            else {
                message.getMessageProperties().setHeader("previous-exchange", returnedMessage.getExchange());
                message.getMessageProperties().setHeader("previous-routingKey", returnedMessage.getRoutingKey());
                rabbitTemplate.convertAndSend("exchange-spare", "routingKey", returnedMessage.getMessage());
            }
        });
    }
}
```
```java
@RestController
@RequestMapping("/send")
public class MqSentController {
    @Autowired
    private MessageSender messageSender;
    @RequestMapping("/demo")
    public String direct1() {
        this.messageSender.rabbitConvertAndSend("exchange-demo", "demo", new Book(1, "chenqf", "haha"));
        return "demo";
    }
}
```

### 接收方确认

RabbitMq 提供2中方式确认消息(auto / manual)

> 自动确认模式下，消息发送后立即认为已成功发送。这种模式以更高的吞吐量（只要消费者能够跟上）为代价，以降低交付和消费者处理的安全性。
> 
> 使用手动确认时，当发生传递的通道（或连接）关闭时，任何未确认的传递（消息）都会自动重新排队。这包括客户端的 TCP 连接丢失、消费者应用程序（进程）故障和通道级协议异常

经测验, 使用原生rabbit客户端sdk验证自动模式, 存在大量消息被客户端获取, 再未处理完成时服务停止, 导致剩余消息未消费就丢失的情况

```java
public class Demo {
    public static void main(String[] args) throws IOException, TimeoutException {
        Map<String, String> envVariables = System.getenv();
        String hostname = envVariables.get("ENV_CLOUD_IP");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("chenqf");
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received message: " + message);
            }
        };
        channel.basicConsume("classic-demo", true, consumer); // 自动模式
    }
}
```

springBoot2.6.13中, 验证使用spring-rabbit中`@RabbitListener`的方式消息消息并指定自动应答模式, 并未出现以上问题

验证结果: 

+ 大量消息积压时,部分消息被spring客户端拉去,消息状态变为`Unacked`,消息未完全处理时服务停止, 剩余消息重新入队
+ 单一消息被消费时, 业务代码出现异常, 当前消息重新入队

```java
@Service
public class BookService {
  @RabbitListener(queues = "classic-demo",ackMode = "AUTO")
  public void receiveAck1(String msg,Message message,Channel channel) throws Exception {
    throw new Exception("一个异常");
  }
}
```

当异常时重试消费消息, 超过重试次数,消息进入死信队列

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        retry:
          enabled: true          # 是否开启重试
          max-attempts: 5        # 最大重试次数
          max-interval: 50000    # 重试最大间隔时间
          initial-interval: 1000 # 重试间隔（单位：毫秒）
          multiplier: 2          # 间隔时间乘子，间隔时间*乘子=下一次的间隔时间，最大不能超过设置的最大间隔时间
```

手动ACK实现:

```java
@Service
public class BookService {
  @RabbitListener(queues = "quorum-demo",ackMode = "MANUAL")
  @RabbitHandler
  public void receiveAck2(Book book,Message message,Channel channel) {
    // 当前第几次接收到该消息 deliveryCount 从0开始
    Object o = message.getMessageProperties().getHeaders().get("x-delivery-count");
    long deliveryCount = o == null ? 0 : (long) o;
    System.out.println("第" + deliveryCount + "次收到消息:" + book);
    if(deliveryCount >= 5){
      // 拒绝, 丢弃消息进入死信队列
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);
      // 应答, 消息消费成功
      channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }else{
      // 拒绝, 并重新入队
      channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,true);
    }
  }
}
```

## Other

### 有序消费 - 单活模式

**单活模式**

```java
@Configuration
public class AmqpConfig {
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Bean
    public Queue singleActiveQueue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-single-active-consumer",true); // 单活模式队列
        map.put("x-queue-type","quorum"); // 指定为 quorum 类型的队列
        Queue queue = new Queue("single-active-demo",true,false,false,map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }
}
```

![image-20230723142518248](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230723142518248.png)

一个队列只有一个消费者 , 缺点: 无法并发消费消息, 并发能力大大下降

实际业务中, 更可能的情况是同业务实体的消息需要有序的, 可根据业务实体的`id进行取模`, 根据结果拆分`多个单活队列`, 提高并发能力

### 批量消费

默认每次消费一条消息, 对于某些业务场景, 效率较低, 可通过@RabbitListener中的concurrency参数设定批量消费

```java
@Service
public class BookService {
  @RabbitListener(queues = "classic-demo",concurrency = "5")
  public void receive(List<Book> books,List<Message> messages, Channel channel) throws Exception {
    for (Book book : books) {
      System.out.println(book);
    }
  }
}
```

### 避免重复消费

消费消息时,业务逻辑已完成, 在应答前宕机, 会导致消息未被删除, 会出现重复消费现象

TODO 


### 幂等消费



+ 幂等消费
+ 事务 https://www.bilibili.com/video/BV14N411c74U/?spm_id_from=trigger_reload&vd_source=0494972cf815a7a4a8ac831a4c0a1229

+ 联邦插件
+ 分片插件

+ 按消息键保序
+ 
并发消费 https://www.bilibili.com/video/BV1vY411671c/?spm_id_from=333.337.search-card.all.click&vd_source=0494972cf815a7a4a8ac831a4c0a1229

+ 批量发送 https://www.bilibili.com/video/BV16T411a7q4/?spm_id_from=autoNext&vd_source=0494972cf815a7a4a8ac831a4c0a1229


## Rabbit Plugin

## Production 最佳实践


+ RabbitMq分发日志
+ RabbitMq动态监听
+ 分布式事务最终一致性