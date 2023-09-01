# 任务 - TASK

## 异步任务@Async

+ 线程池问题 - `不建议使用, 建议使用MQ实现`
  + 默认使用Spring的默认线程池来执行异步任务。 
  + 如果不进行适当的配置，可能会导致线程池过载或资源不足的问题
+ 链路追踪失效

**配置自定义线程池, 并开启异步任务**

```java
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {
    @Bean("asyncTask")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 可用处理器数量,几核
        int i = Runtime.getRuntime().availableProcessors();
        //核心线程数目
        executor.setCorePoolSize(i * 2);
        //指定最大线程数
        executor.setMaxPoolSize(i * 4);
        //队列中最大的数目
        executor.setQueueCapacity(i * 2 * 10);
        //线程名称前缀
        executor.setThreadNamePrefix("custom-async-");
        //对拒绝task的处理策略 CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //当调度器shutdown被调用时等待当前被调度的任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //线程空闲后的最大存活时间
        executor.setKeepAliveSeconds(60);
        //加载
        executor.initialize();
        return executor;
    }
}
```

**标识方法为@Async并指定线程池, 非阻塞执行**
```java
@Service
public class AsyncService {
    @Async("asyncTask")
    public void test1() throws InterruptedException {
        System.out.println("数据处理中...");
        Thread.sleep(3000);
        System.out.println("数据处理完成...");
    }
}
```
**获取异步任务返回值, 并捕获异常**

```java
@Slf4j
@Service
public class AsyncService {

    @Async("asyncTask")
    public CompletableFuture<String> asyncHandler() {
        // 异步执行的任务逻辑
        String result = "Async task completed";
        int i = 1/0 ;
        return CompletableFuture.completedFuture(result);
    }
}
```

```java
@Slf4j
@RestController("/demo")
public class AsyncController {

    @Autowired
    private AsyncService asyncService;

    @GetMapping("/test1")
    public String test1() {
        try {
            CompletableFuture<String> asyncResult = asyncService.asyncHandler();
            String result = asyncResult.join(); // 阻塞等待异步任务完成并获取返回值
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
}
```

## 单机定时任务@Scheduled

+ 默认是单线程执行的
  + 长时间运行的任务可能会影响其他任务的执行时间后移
  + 如果两个任务的执行时间重叠，可能会导致执行延迟或任务堆积
+ 不支持集群存在任务重复执行的问题
  + 分布式锁勉强处理
+ 不重启服务修改代码无法关闭和开启
+ 不重启服务修改代码无法修改任务时间参数
+ 处理大量数据时无法利用集群进行分片处理
+ 不支持失败重试

**多线程-方案1: 指定线程池**

```java
@Configuration
@EnableScheduling
public class ScheduleConfig {
  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    //最大线程数
    taskScheduler.setPoolSize(30);
    taskScheduler.setThreadNamePrefix("custom-schedule-");
    return taskScheduler;
  }
}
```

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

**多线程-方案2: 结合@Async**

```java
@Service
public class ScheduleService {
  @Async
  @Scheduled(cron = "30 * * * * *") // 每分钟的第30秒
  public void ScheduleMethod1() throws InterruptedException {
    System.out.println(new Date() + "  :定时任务..");
    Thread.sleep(5000);
  }
}
```

## XXL-Job




