# 任务 - TASK

## 异步任务

#### 开启异步任务 @EnableAsync
```java
@SpringBootApplication
@EnableAsync
public class TaskApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(TaskApplication.class, args);
    }
}
```

#### 标识方法为异步 @Async
```java
@Service
public class AsyncService {
    @Async
    public void test1() throws InterruptedException {
        System.out.println("数据处理中...");
        Thread.sleep(3000);
        System.out.println("数据处理完成...");
    }
}
```

#### 使用 - 立即返回
```java
@RestController
public class DemoController {

    @Autowired
    private AsyncService asyncService;

    @GetMapping("/async")
    public Result test1() throws InterruptedException {
        asyncService.test1();
        return Result.success();
    }
}
```

## 单机定时任务

#### 开启定时任务 @EnableScheduling
```java
@SpringBootApplication
@EnableScheduling
public class TaskApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(TaskApplication.class, args);
    }
}
```

#### 标识方法为定时任务 @Async
```java
@Service
public class ScheduleService {
    // 秒 分 时 日 月 周几
    @Scheduled(cron = "30 * * * * *") // 每分钟的第30秒
    public void ScheduleMethod(){
        System.out.println(new Date() + "  :定时任务..");
    }
}
```

### 动态时间定时任务

TODO

> 此种方案仅适用于单机部署, 对于集群部署会出现多服务均触发任务

## 集群下定时任务

TOOD

### 动态时间定时任务

TODO 

## 邮件任务

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

```yaml
spring:
  application:
    name: task-demo
  mail:
    password: ${ENV_MAIL_USERNAME}
    username: ${ENV_MAIL_PASSWORD}
    host: smtp.qq.com
    port: 465
    properties:
      mail:
        smtp:
          ssl:
            enable: true
```

```java
@RequestMapping("/mail")
@RestController
public class MailController {

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String mailName;

    @GetMapping("/send")
    public Result send(){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("spring 邮件主题");
        mailMessage.setText("spring 邮件内容");
        mailMessage.setFrom(mailName); // 谁发的
        mailMessage.setTo(mailName); // 发给我自己
        javaMailSender.send(mailMessage);
        return Result.success();
    }

    @GetMapping("/mimeSend")
    public Result mimeSend() throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        helper.setSubject("复杂邮件主题");
        helper.setText("<div>html格式的正文</div>",true);
        helper.addAttachment("附件名.jpg",new File("filepath"));
        helper.setFrom(mailName); // 谁发的
        helper.setTo(mailName); // 发给我自己
        javaMailSender.send(mimeMessage);
        return Result.success();
    }
}
```
