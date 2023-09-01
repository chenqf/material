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