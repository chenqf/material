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

## 定时任务

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