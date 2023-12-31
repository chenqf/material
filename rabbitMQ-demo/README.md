# RabbitMQ

可靠性高, 功能全面, 消息基本不会丢, 吞吐量比较低(2-3W/s), 消息积累影响性能

> 提升系统异步通信、扩展解耦能力

一条消息, 不能被多个客户端消费, 只能一个客户端消费

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

## Exchange - 路由

Exchange绑定Queue时会指定RoutingKey, 一个Exchange可以绑定多个Queue, 也可以将消息转发给其他Exchange

生产者发送消费给Exchange, 根据Exchange类型和发送的RoutingKey, Exchange将消息转发给不同的队列

Exchange分类:

+ direct : 消费者发送的RoutingKey和BindingKey完全相同, 才会把消息转发到对应的对
+ fanout : Exchange会把消息广播到已绑定的所有Queue
+ topic  : 消费者发送的RoutingKey满足BindingKey就可以将消息发送到Queue中

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
    @RabbitListener(queues = "classic-demo", ackMode = "AUTO")
    public void receiveAck1(String msg, Message message, Channel channel) throws Exception {
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
    @RabbitListener(queues = "quorum-demo", ackMode = "MANUAL")
    @RabbitHandler
    public void receiveAck2(Book book, Message message, Channel channel) {
        // 当前第几次接收到该消息 deliveryCount 从0开始
        Object o = message.getMessageProperties().getHeaders().get("x-delivery-count");
        long deliveryCount = o == null ? 0 : (long) o;
        System.out.println("第" + deliveryCount + "次收到消息:" + book);
        if (deliveryCount >= 5) {
            // 拒绝, 丢弃消息进入死信队列
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);
            // 应答, 消息消费成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            // 拒绝, 并重新入队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
```

## Other

### 多线程消费

程序默认开启一条线程对queues进行监听, 可配置concurrency指定当前进程下开启几条线程对queues进行监听

```java

@Service
public class BookService {
    @RabbitListener(queues = "queue-demo1", concurrency = "3")
    public void receive(Book book, Channel channel) throws Exception {
        System.out.println("---------------------------");
        System.out.println("消费:" + book + "-thread:" + Thread.currentThread().getId());
        System.out.println("---------------------------");
    }
}
```

### 批量消费

@RabbitListener中的containerFactory, 配置消息监听容器工程, 配置一次处理多条消息, 提高消费速度

**配置批量消费Bean:**

```java

@Configuration
public class BatchConfig {
    @Autowired
    ConnectionFactory connectionFactory;
    @Autowired
    SimpleRabbitListenerContainerFactoryConfigurer configurer;

    @Bean
    public SimpleRabbitListenerContainerFactory consumerBatchContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        // 配置消费者的监听器是批量消费消息的类型
        factory.setBatchListener(true);
        // 一批几个
        factory.setBatchSize(5);
        // 等待时间 毫秒 , 这里其实是单个消息的等待时间 指的是单个消息的等待时间
        // 也就是说极端情况下，你会等待 BatchSize * ReceiveTimeout 的时间才会收到消息
        factory.setReceiveTimeout(3 * 1000L);
        factory.setConsumerBatchEnabled(true);
        return factory;
    }
}
```

**指定批量消费:**

```java

@Service
public class BookService {
    @RabbitListener(queues = "classic-demo", concurrency = "1", containerFactory = "consumerBatchContainerFactory")
    public void receive(List<Book> books, List<Message> messages, Channel channel) throws Exception {
        System.out.println(books.size() + ":thread-id: " + Thread.currentThread().getId());
        for (Book book : books) {
            System.out.println(book);
        }
    }
}
```

### 批量发送

批量发送消息，可以提高MQ发送性能, 达到以下限制则批量发送消息:

+ batchSize ：超过收集的消息数量的最大条数。
+ bufferLimit ：超过收集的消息占用的最大内存。
+ timeout ：超过收集的时间的最大等待时长，单位：毫秒。
    + 是以最后一次发送时间为起点。也就说，每调用一次发送消息，都以当前时刻开始计时

```java

@Configuration
public class BatchConfig {
    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    SimpleRabbitListenerContainerFactoryConfigurer configurer;

    @Bean
    public BatchingRabbitTemplate batchRabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {

        int batchSize = 10;
        // 每次批量发送消息的最大内存 b
        int bufferLimit = 1024 * 1024;
        // 超过收集的时间的最大等待时长，单位：毫秒
        int timeout = 5 * 1000;
        BatchingStrategy batchingStrategy = new SimpleBatchingStrategy(batchSize, bufferLimit, timeout);
        // 创建 TaskScheduler 对象，用于实现超时发送的定时器
        TaskScheduler taskScheduler = new ConcurrentTaskScheduler();
        // 创建 BatchingRabbitTemplate 对象
        BatchingRabbitTemplate batchTemplate = new BatchingRabbitTemplate(batchingStrategy, taskScheduler);
        // 指定json序列化
        batchTemplate.setMessageConverter(new Jackson2JsonMessageConverter(objectMapper));
        batchTemplate.setConnectionFactory(connectionFactory);
        return batchTemplate;
    }
}
```

```java

@RestController
@RequestMapping("/send")
public class MqSentController {

    @Autowired
    private BatchingRabbitTemplate batchingRabbitTemplate;

    @RequestMapping("/batch/demo")
    public String batch() {
        for (int i = 0; i < 20; i++) {
            this.batchingRabbitTemplate.convertAndSend("batch-exchange", "demo", new Book(1, "chenqf", "haha" + i));
        }
        return "success";
    }
}
```

虽然多条消息一次性发送, 但在rabbit后台看到的仅为单条消息, 在消息体的header中amqp_batchSize字段标识当前批量了几条消息

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
        map.put("x-single-active-consumer", true); // 单活模式队列
        map.put("x-queue-type", "quorum"); // 指定为 quorum 类型的队列
        Queue queue = new Queue("single-active-demo", true, false, false, map);
        amqpAdmin.declareQueue(queue);
        return queue;
    }
}
```

![image-20230723142518248](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230723142518248.png)

一个队列只有一个消费者 , 缺点: 无法并发消费消息, 并发能力大大下降

无论是集群下多个进程下的线程监听, 还是一个进程下的多个线程进行监听, 只有有一个线程的监听器是Active状态

实际业务中, 更可能的情况是同业务实体的消息需要有序的, 可根据业务实体的`id进行取模`, 根据结果拆分`多个单活队列`, 提高并发能力

### 临时队列

RabbitMq中队列的消息, 被消费一次就会被删除

但实际业务中, 可能存在集群环境下每个客户端节点都都要消费该消息的情况, 且集群节点数不确定

比如多级缓存架构中, 某个节点进程内缓存更新, 需要通知其他所有节点更新进程内缓存

使用临时队列处理, 只有客户端存在时, 临时节点存在, 客户端断开连接, 临时队列自动删除

```java

@Configuration
public class AmqpConfig {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Bean
    public Queue tempQueue() {
        // 创建一个临时队列
        Queue queue = new AnonymousQueue();
        FanoutExchange exchange = new FanoutExchange("process-cache-exchange");
        Binding binding = BindingBuilder.bind(queue).to(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareBinding(binding);
        return queue;
    }
}
```

```java

@Service
public class CacheService {
    @RabbitListener(queues = "#{tempQueue.name}")
    public void listenToTemporaryQueue(String message) {
        // 处理接收到的消息
        System.out.println("Received message from temporary queue: " + message);
    }
}
```

### 幂等消费 - 避免重复消费

1. 发送消息时, 已经落盘, 但由于网络原因, 未给出应答, 导致重复发送
2. 消费消息时,业务逻辑已完成, 在应答前宕机, 会导致消息未被删除, 会出现重复消费现象

消费消息时, 利用业务id或唯一id,存储至redis, 消费前进行查询, 若存在代表已经消费过

消费前,使用分布式锁, 避免并发时时出现重复消费

### 分布式事务

> 最终一致性, 可接受暂时不一致

1. Producer投递消息成功, Consumer消费失败, Producer不需要回滚
2. Consumer消费失败, Consumer手动ACK进行补偿重试, 注意`幂等性`问题
3. Producer投递成功, Consumer消费成功, 但Producer的DB事务回滚了, 需要执行`补单操作`

`补单机制` : 投递消息时分发为两个队列, 一个作为真是Consumer的消费队列, 另一个作为校验是否Producer事务执行成功, 做失败进行补单

TODO

### 联邦插件

在非集群下将消息备份至其他RabbitMQ, 对于`异地多活`的部署比较有用

```shell
# 所有rabbitmq中开启联邦插件
rabbitmq-plugins enable rabbitmq_federation
rabbitmq-plugins enable rabbitmq_federation_management
```

![image-20230724114621928](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230724114621928.png)

Downstream中配置要拉取的Upstream信息:

![image-20230724145527372](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230724145527372.png)

Downstream中配置接收Upstream消息的Policy:

![image-20230724145911451](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230724145911451.png)

检查配置是否成功:

![image-20230724145934300](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230724145934300.png)

配置成功后, Upstream中的新增消息都会同步到Downstream中, 无论是否已经被消费

对于寄存的消息, 无法同步

## TODO

事务消息
