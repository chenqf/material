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

