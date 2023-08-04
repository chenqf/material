# JUC

+ sleep: 即不获取锁也不释放锁
+ wait:  会释放锁, 调用的前提是当前线程占有锁(代码要求在synchronized)
  + 在哪里睡,就在哪里醒

+ 并发: 同一时刻多个线程访问同一资源
+ 并行: 多项工作一起执行, 之后再汇总

+ 用户线程: 自定义线程
+ 守护线程: 特殊线程, 运行在后台, 比如GC

只要没有`用户线程`在执行, JVM就会结束

## 多线程编程步骤

1. 加锁
2. 判断 (判断失败-等待)
3. 干活
4. 通知其他线程

### 虚假唤醒

wait 永远放到 while 中, 醒来也需要再次判断


## 不安全的集合

使用 Vector 替换 ArrayList (不常用)

使用 Collections 解决 (不常用)

使用 CopyOnWriteArrayList (常用)

## 写时复制

+ CopyOnWriteArrayList
+ CopyOnWriteArraySet
+ ConcurrentHashMap

1. 支持并发读
2. 独立写
   1. 复制一份
   2. 写入新内容
   3. 合并老内容和新内容

## 多线程锁

### Synchronized

+ 放到`对象方法`上, 锁在`调用对象`上
+ 放到`静态方法`上, 所在`类本身`上
+ 同步`方法块', 锁在`配置的对象`上

### 非公平锁 - NonFairLock

+ Synchronized
+ new ReentrantLock()

> 效率高
> 线程可能被饿死

### 公平锁 - FairLock

+ new ReentrantLock(true)

> 按进入顺序排队
> 效率相对低

### 可重入锁

+ Synchronized
+ Lock (ReentrantLock)

### 死锁

```shell
jps -l
jstack 进程id
```

## 创建简称的方式

1. 继承 Thread - 无法返回结果
2. 实现 Runnable - 无法返回结果
3. Callable
   + 有返回值
   + 有异常抛异常
   + 实现是call方法
4.

### Callable 接口

#### 未来任务 - FutureTask 

```java
public class ThreadDemo5 {

   public static void main(String[] args) throws InterruptedException, ExecutionException {
      FutureTask<Integer> futureTask = new FutureTask<>(() -> {
         System.out.println(Thread.currentThread().getName() + " come in callable");
         return 1024;
      });

      new Thread(futureTask,"t1").start();

      while (!futureTask.isDone()){
         System.out.println("wait ....");
         Thread.sleep(20);
      }
      System.out.println("result: " + futureTask.get());
      System.out.println("result: " + futureTask.get());
      System.out.println(Thread.currentThread().getName() + " come over");
   }
}
```

## 辅助类

### CountDownLatch

```java
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            new Thread(()->{
                System.out.println("离开");
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        System.out.println("锁门");
    }
}
```

### CyclicBarrier

```java
public class CyclicBarrierDemo {
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
            System.out.println("7颗龙珠被收集了");
        });
        for (int i = 1; i <= 7; i++) {
            int finalI = i;
            new Thread(()->{
                System.out.println(finalI + "被收集了");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }
        for (int i = 1; i <= 7; i++) {
            int finalI = i;
            new Thread(()->{
                System.out.println(finalI + "被收集了");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }
    }
}
```

### Semaphore

```java
public class SemaphoreDemo {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);
        for (int i = 1; i <=6; i++) {
            new Thread(()->{
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName() + " 抢到车位");
                    TimeUnit.SECONDS.sleep(new Random().nextInt(5));// 随机停车时间
                    System.out.println(Thread.currentThread().getName() + " 离开车位");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }
            },String.valueOf(i)).start();
        }
    }
}
```

## 阻塞队列 BlockingQueue

队列: 先进先出
栈:  后进先出

队列为空, 获取元素会阻塞 - 自动
队列为慢, 插入元素会阻塞 - 自动

### ArrayBlockingQueue


### linkedBlockingQueue