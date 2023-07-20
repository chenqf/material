### 为什么几乎很少使用Timer这种方式？
Timer底层是使用一个单线来实现多个Timer任务处理的，所有任务都是由同一个线程来调度，所有任务都是串行执行，意味着同一时间只能有一个任务得到执行，而前一个任务的延迟或者异常会影响到之后的任务。
如果有一个定时任务在运行时，产生未处理的异常，那么当前这个线程就会停止，那么所有的定时任务都会停止，受到影响。


### 分布式任务

+ Quartz Cluster 
+ XXL-Job
+ Elastic-Job


### 多线程事务

https://www.bilibili.com/video/BV1v8411E7Kh/?spm_id_from=333.337.search-card.all.click&vd_source=0494972cf815a7a4a8ac831a4c0a1229

https://www.bilibili.com/video/BV1Td4y1b7DQ/?spm_id_from=333.337.search-card.all.click&vd_source=0494972cf815a7a4a8ac831a4c0a1229


### 时间轮算法

### MQ延时消息

异步 VS 非阻塞

极限 1.2W+ 