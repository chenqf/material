# 运行时数据区 - Runtime Data Area

![image-20230907104016873](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907104016873.png)

**进程内所有线程共享:**

1. 方法区(元空间) - Method Area
2. 堆 - heap

**线程内独立:**

1. 程序计数器 - Program Counter Register
2. 本地方法栈 - Native Method Stack
3. 虚拟机栈 - VM Stack

> Hotspot JVM中, 每个线程都与操作系统的本地线程直接映射

![image-20230907104209497](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907104209497.png)

## 程序计数器 Program Counter Register

![image-20230907122750183](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907122750183.png)

PC寄存器用来存储指向下一跳指令的地址, 也即将要执行的指令代码, `可以想象成一个游标`

+ 很小的内存空间, 可忽略不计
+ 每个线程都有自己的PC寄存器, 线程私有, 声明周期与线程一直
+ 任何时间一个线程只有一个方法在执行(`当前方法`), PC寄存器会存储`当前方法`的JVM指令地址
+ 没有GC, 没有OOM

![image-20230907124913152](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907124913152.png)

CPU需要不停的切换各个线程, 切换回来以后, 需要知道从哪开始继续执行

## 虚拟机栈 - VM Stack

> 栈解决程序运行的问题
>
> 堆解决数据存储的问题

每个线程一个`VM Stack`, 其内保存一个一个的`栈帧`, 每个`栈帧`对应一个个的`方法`, 对于栈来说不存在`GC`

如果请求分配的栈容量超过JVM允许的最大容量会抛出`StackOverflowError`

可以使用 `-Xss` 来设置线程中的最大栈空间, `不推荐更改`

![image-20230907212750359](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907212750359.png)

![image-20230907213015611](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907213015611.png)

**栈帧其中包含:**

+ **`局部变量表`** - Local Variables
+ **`操作数栈`**
+ 动态链接
+ 方法返回地址

### 局部变量表 Local Variables

定义一个数字数组, 用于存储方法参数和定义在方法体内的局部变量

其内最基本的存储单元为`Slot(插槽)`

> byte、short、char在存储前转换为int , 只占`一个Slot`
>
> boolean也转换为int, true->非0 / false -> 0 , 只占`一个Slot`
>
> long 和 double 则占据`两个Slot`

局部变量表的容量大小是在编译器确定的

**通过`jclasslib`反编译解析:**

![image-20230907215117212](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907215117212.png)

局部变量表存储了所有的局部变量以及每个变量在字节码中的作用域范围![image-20230907214805830](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907214805830.png)

字节码:![image-20230907215018646](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907215018646.png)

java代码与字节码的对应关系:

![image-20230907215435081](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907215435081.png)

对于`构造方法`或者`实例方法`创建的`栈帧`, `局部变量表`中的`第一个slot`会用来存放`this`

![image-20230907220111191](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907220111191.png)

局部变量表中的`Slot`是可以`重用`的, 当变量超过了作用域, 其后申明的变量可复用之前的`Slot`

![image-20230907220457528](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907220457528.png)

### 操作数栈 Operand Stack

在方法执行过程中, 根据字节码指令, 像栈中写入数据或提取数据, 即入栈和出栈

主要用于保存计算过程的中间结果, 同时作为计算过程中变量临时的存储空间

每一个操作数栈都有一个明确的栈深度, max_stack的值是在编译器确定的

```java
public class OperandStackTest {
    public static void main(String[] args) {
        byte i = 15;
        int j = 8;
        int k = i + j;
    }
}
```

**以上代码是按如下方式执行的:**

![image-20230907221546791](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907221546791.png)

![image-20230907221751892](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907221751892.png)

![image-20230907221845871](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907221845871.png)

![image-20230907221930176](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907221930176.png)

#### 栈顶缓存

操作数是存储在内存中的, 频繁的内存读写必然影响速度

栈顶缓存技术, 将栈顶元素全部缓存在`物理CPU`的`寄存器`中, 以降低内存的读写次数

### 帧数据区 - 动态链接

> 指向运行时常量池的方法引用

每个栈帧内部都包含一个指向`运行时常量池`中`该栈帧所属方法的引用`

Java源文件编译为字节码文件时, `变量和方法引用`都作为`符号引用`保存在`class文件的常量池中`

**动态链接的作用就是将这些符号引用转换为调用方法的直接引用**

![image-20230907224243557](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907224243557.png)

![image-20230907224312778](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907224312778.png)

#### 方法的调用

绑定是一个`字段、方法或者类`在`符号引用`被替换为`直接引用`的过程, 这仅发生一次

**早期绑定:** 目标方法在编译期可知, 且运行期保持不变, 明确了被调用的目标方法究竟是哪一个

**晚期绑定:** 被调用的方法在编译期无法被确定下来, 只能在程序运行期根据实际的类型绑定相关的方法

```java
public class BindTest {

    interface Huntable{
        void hunt();
    }

    class Animal{
        public void eat(){

        }
    }

    class Cat extends Animal implements Huntable{

        public Cat() {
            super(); // 早期绑定
        }
        public Cat(String name){
            this(); // 早期绑定
        }

        @Override
        public void hunt() {

        }

        @Override
        public void eat() {

        }
    }

    public void showAnimal(Animal animal){
        animal.eat(); // 晚期绑定
    }

    public void showHunt(Huntable h){
        h.hunt(); // 晚期绑定
    }

}
```

**非虚方法:**

好好研究多态之后, 再回来: https://www.bilibili.com/video/BV1PJ411n7xZ?p=57&spm_id_from=pageDriver&vd_source=0494972cf815a7a4a8ac831a4c0a1229

如果编译期就确定了具体的调用版本, 版本在运行期不可变, 则称为`非虚方法`

+ 静态方法
+ 私有方法
+ final方法
+ 实例构造器
+ 父类方法

**非虚方法:**

在ClassLoader中的Resolve阶段确定符号引用对应的直接引用, 并生成虚方法表

#### 调用指令

1. `invokestatic : 调用静态方法` - 非虚方法
2. `invokespecial: 调用<init>方法、私有及父类方法, 解析阶段确定唯一方法版本` - 非虚方法
3. invokevirtual: 调用所有虚方法
4. invokeinterface: 调用接口方法
5. invokedynamic: 动态调用指令, 动态解析出所需要调用的方法, 然后执行

### 帧数据区 - 方法返回地址

存储调用者的PC寄存器的值

用于在当前方法执行完成后, 拿到调用者的PC寄存器值, 继续执行

**退出方法的两种方式:**

1. 正常退出: 执行完成, 基于方法返回地址
2. 异常退出: 遇到异常, 没有在方法内进行处理(异常表中没有匹配), 导致方法退出

****

从VM Stack来看线程是否安全:

```java
public class ThreadSafeTest {

    // 线程安全
    public static void method1(){
        StringBuilder builder = new StringBuilder();
        builder.append("s");
        builder.append("s");
    }


    // 线程不安全
    public static void method2(StringBuilder builder){
        builder.append("s");
        builder.append("s");
    }

    // 线程不安全
    public static StringBuilder method3(){
        StringBuilder builder = new StringBuilder();
        builder.append("s");
        builder.append("s");
        return builder;
    }
}
```

## 本地方法栈 Native Method Stack

### 本地方法 Native Method

> 基本不用

就是一个Java调用非Java代码的接口

使用`native`修饰的方法, 就是非Java实现的方法, 就是一个`本地方法`

**何时使用:**

1. 与Java环境外交互
2. 与操作系统交互

### 什么是本地方法栈

VM Stack用于管理Java方法的调用, 本地方法栈用于管理本地方法的调用

1. 线程私有
2. 栈容量可设置

> Hotspot JVM中, 直接将本地方法栈和虚拟机栈合二为一

## 堆 Heap

进程内唯一, 多线程共享, JVM启动时候即被创建, 其空间大小也就确定了

堆处于物理上不一定连续的内存空间中, 但在逻辑上是连续额的

方法执行结束, 栈中内存被销毁, 但栈中的对象引用不会立即销毁, 要等到下一次GC

+ -Xms: 初始堆空间大小 = -XX:InitialHeapSize
+ -Xmx: 最大堆空间大小 = -XX:MaxHeapSize
+ `-XX:+PrintGCDetails` : 打印堆内存详情 

现代GC都基于分代收集理论, Java8+以后, 内存逻辑上分为三部分: 

+ 新生区
+ 养老区
+ `元空间`, Java7叫做永久区

新生区内存 + 养老区内存 = Xms/Xmx

### 堆内存大小与OOM

> 本地工具: Java VisualVM (JDK8内置)

+ -Xms: 初始堆空间大小 = -XX:InitialHeapSize
  + 默认: 物理内存 / 64
+ -Xmx: 最大堆空间大小 = -XX:MaxHeapSize
  + 默认: 物理内存 / 4

当堆区内存超过`-Xmx`时, 则抛出`OutOfMemoryError`

通常会将`-Xms`和`-Xmx`两个参数配置`相同`的值, 其目的是在GC结束后不需要重新计算堆区的大小, 避免扩容缩容

```java
public class HeapTest {
  public static void main(String[] args) {
    // -Xms
    long l = Runtime.getRuntime().totalMemory();
    // -Xmx
    long n = Runtime.getRuntime().maxMemory();
  }
}
```

**查看当前运行的Jvm进程:**

```shell
Jps
```

### 年轻代与老年代

![image-20230911132246621](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911132246621.png)

新生代和老年代在堆结构的占比(一般不会调整):

设置`-XX:NewRatio=2`, 表示新生代占1, 老年代占2, 新生代占整个堆的1/3, `默认为2`

设置`-XX:SurvivorRatio=8`, 表示Eden占8, Survivor0和Survivor1占1

![image-20230911133840239](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911133840239.png)

> 几乎所有new出来的对象都是在`Eden`中创建的

> 绝大多数对象的销毁都是在`新生代`中进行的

**查看某个Jvm进程内存使用情况:**

```shell
jstat -gc <pid>
```

![image-20230911130538695](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911130538695.png)

+ OC: 老年代总量
+ OU: 老年代使用量
+ EC: 新生代中伊甸园区总量
+ EU: 新生代中伊甸园区使用量
+ S0C: 新生代中S0区总量
+ S0U: 新生代中S0区使用量
+ S1C: 新生代中S0区总量
+ S1U: 新生代中S0区使用量

> S0和S1同时只有一个区域被使用

### 对象分配过程

> Eden满了会触发Minor GC
>
> Survivor满了不会触发Minor GC

![image-20230911134337251](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911134337251.png)

1. 对象创建在`Eden`中
2. 当`Eden`满了, 进行`Minor GC`
3. `Eden`中销毁不使用的对象
4. 将`Eden`中依然使用的对象放入`Survivor0`中
5. `Survivor0`中的对象添加一个`年龄计数器`

![image-20230911134731281](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911134731281.png)

6. 当`Eden`又满了, 进行`Minor GC`
7. `Eden`和`Survivor`中销毁不使用的对象
8. 将`Eden`中依然使用的对象放入`Survivor1`中
9. 将`Survivor0`中依然使用的对象放入`Survivor1`中
10. 指定`Survivor1`中对象的年龄计数器

![image-20230911135235652](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911135235652.png)

11. 当`Survivor`中对象的计数器达到`阈值`, 将对象晋升到`老年代`中
    + 阈值默认15 , 通过 -XX:MaxTenuringThreshold=<N> 进行设置

********

![image-20230911140054165](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911140054165.png)

### Minor GC / Major GC / Full GC

> 关于垃圾回收: 频繁发生在`新生代`, 很少发生在`养老区`, 几乎不在`元空间`

+ Minor/Yong GC
  + 对于新生代(Eden / Survivor)内存的GC
+ Major GC
  + 对于老年代的垃圾回收
+ Full GC
  + 对整个堆和方法区的垃圾回收

#### Minor/Yong GC

`Eden`满了就会触发`Minor GC`, `Survivor`满了不会触发`Minor GC`

`Minor GC`非常频繁, 一般回收速度也很快

`Minor GC`会引发`STW`, 暂停其他用户线程, 等GC结束, 用户线程才能恢复

#### Major GC

出现`Major GC`, 经常会伴随至少一次的`Minor GC`. 当老年代空间不足时, 先尝试`Minor GC`, 再尝试`Major GC`

`Major GC`时间一般比`Minor GC`慢10倍以上, `STW`的时间更长

如果`Major GC`后, 内存还不足, 就`OOM`了

#### Full GC

调用`System.gc()`, 系统建议执行`Full GC`, 但不是必然执行

老年代空间不足会触发

方法区空间不足会触发

#### 堆的空间分代思想

> 分代的唯一理由就是`优化GC性能`, 经研究70%-99%的对象时临时对象

![image-20230911153618884](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911153618884.png)

![image-20230911153740498](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911153740498.png)

### 内存分配策略

优先分配到`Eden`

`大对象`直接分配到`老年代`

+ 大对象大于`Eden`总容量也大于`Survivor`总容量, 直接进入`老年代`

`Survivor`中年龄大于`阈值`的对象进入`老年代`

+ `Survivor`中的对象每经过一次`Minor GC`, 则`年龄加1`

动态年龄判断:

+ 如果`Survivor`中相同年龄的所有对象的总和大于`Survivor`空间的一半
+ 年龄大于等于该年龄的对象可以直接进入`老年代`

#### 分配内存: TLAB(Thread Local Allocation Buffer)

**为什么要有TLAB**

堆区是线程共享区域, 任何线程都可以放到到堆区的共享数据

为避免多个线程操作同一地址, 需要使用加锁机制, 进而影响分配速度

**什么是TLAB**

从内存模型的角度, 对`Eden`进行划分, JVM对每个线程分配了一个私有缓存区域

多线程分配内存是, 使用`TLAB`可以避免一系列的`非线程安全`问题 `(快速分配策略)`



![image-20230911155207049](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911155207049.png)

JVM将TLAB是内存分配的首选

可通过`-XX:UseTLAB` 进行开启, 默认开启

默认`TLAB`空间内存很小, 只占整个`Eden`空间的`1%`, 可通过-XX:TLABWasteTargetPercent

若在`TLAB`分配内存失败, JVM会尝试通过`加锁机制`确保数据操作的原子性, 直接在Eden中分配内容

![image-20230911155804515](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911155804515.png)

#### 逃逸分析

1. 当一个对象在方法中被定义后, 对象只在方法内使用, 则没有发生逃逸
2. 当一个对象在方法中被定义后, 它被外部方法所引用, 则认为发生了逃逸

> 是否逃逸和是否线程安全是一致的

> 将堆上的对象分配到栈, 需要使用逃逸分析手段(`JVM目前没采用`)

##### 标量替换

经过逃逸分析, 发现一个对象不会被外界访问, 经过JIT优化, 会将对象拆分为若干个成员变量来替代

![image-20230911165800705](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911165800705.png)

等价于 --->

![image-20230911165844753](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911165844753.png)

## 方法区 Method Area

逻辑上方法区是堆的一部分, 但对于HotSpotJVM而言, 方法区还有一个别名`非堆`

实际上可以将方法区看做独立于堆的一个区域

方法区主要用于存储`类信息`

元空间是方法区的落地实现

### 栈 & 堆 & 方法区的关系

![image-20230911170533378](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230911170533378.png)

### 方法区大小及OOM

默认是动态调整的, 使用物理内存不使用虚拟机内存

通过-XX:MetaspaceSize=21m 来设置方法区的初始值带下

通过-XX:MaxMetaspaceSize=100m 来设置方法区的最大值, 默认无上限

> 一般设置MetaspaceSize给一个高值, 避免频繁GC, 一般不设置MaxMetaspaceSize

### 方法区内部机构

### 方法区GC

### 案例

