# 类加载机制

当我们用java命令运行某个类的main函数启动程序时，首先需要通过`类加载器`把主类加载到` JVM`

```java
public class Math {
    public static final int initData = 666;
    public static User user = new User();

    public int compute() { //一个方法对应一块栈帧内存区域
        int a = 1;
        int b = 2;
        int c = (a + b) * 10;
        return c;
    }

    public static void main(String[] args) {
        Math math = new Math();
        math.compute();
    }
}
```

**大体流程如下:**

![image-20230921172213974](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230921172213974.png)

## 类加载过程

> **加载** >> **验证** >> **准备** >> **解析** >> **初始化** >> 使用 >> 卸载

![image-20230921173622491](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230921173622491.png)

#### 1. 加载

+ 在硬盘上查找类的全限定名, 并通过IO读入字节码文件 , 使用到类时才会加载，例如调用类的 main()方法，new对象等
+ 将字节流代表的静态存储结构转化为`方法区`的运行时数据结构
+ 内存中生成一个列表这个类的`java.lang.Class对象`, 作为`方法区`中这个类的访问入口

#### 2. 验证

校验字节码文件的正确性

+ 文件格式验证
+ 元数据验证
+ 字节码在验证
+ 符号引用验证

#### 3. 准备

给类的静态变量分配内存，并赋予默认值

+ 类的`非final静态变量`分配内存并设置默认初始值(`零值`)
+ 类的`final静态变量`分配内存并`显示初始化`

> `不会为实例变量分配初始化`, `类变量`分配在`方法区`中, `实例变量`会随对象一起分配到`Java堆`中

#### 4. 解析

将`符号引用`替换为直接引用

该阶段会把一些静态方法(符号引用，比如 main()方法) , 替换为指向数据所存内存的指针或句柄等(直接引用)

这是所谓的`静态链接`过程(类加载期间完成)，`动态链接`是在程序运行期间完成的将`符号引用`替换为`直接引用`

#### 5. 初始化

执行类构造方法`<clinit>()`的过程

+ 对类的`非final静态变量`初始化为指定的值
+ 执行静态代码块

> 执行时按语句在源文件中出现的顺序执行
>
> 若具有父类, 子类`<clinit>()`执行前, 父类`<clinit>()`已经执行完毕

**以下7中情况才会导致类的初始化:**

1. 创建类实例
2. 访问类或接口的静态变量
3. 调用类的静态方法
4. 反射(比如: Class.forName("com.xxx.xx"))
5. 初始化一个类的子类
6. JVM启动时被标明启动类的类
7. JDK提供的动态语言支持

## 类加载器分类

**启动类加载器 Bootstrap Class Loader**

> 负责加载支撑JVM运行的位于JRE的lib目录下的核心类库，比如 rt.jar、charsets.jar等

1. `C/C++`实现, 嵌套在JVM中
2. 用于加载核心类库
3. 不继承java.lang.ClassLoader, 没有父加载器

**扩展类加载器 Extension ClassLoader**

> 负责加载支撑JVM运行的位于JRE的lib目录下的ext扩展目录中的JAR 类包
>
> parent属性指向`Bootstrap ClassLoader`

1. Java编写
2. 派生于ClassLoader
3. 从java.ext.dirs系统属性的目录中加载类库
4. 从JDK的安装目录下的jre/lib/ext下加载类库

**应用程序加载器 App ClassLoader**

> 负责加载ClassPath路径下的类包，主要就是加载你自己写的那些类
>
> parent属性指向`Extension ClassLoader`

1. Java编写
2. 派生于ClassLoader
3. 父类加载器是`Extension ClassLoader`
4. 负责加载classpath或java.class.path下的类库
5. 默认的类加载器
6. 如何获取:`ClassLoader.getSystemClassLoader()`

**自定义加载器**

> 负责加载用户自定义路径下的类包
>
> parent属性指向`App ClassLoader`

主要用途: 

+ `隔离加载类` eg: 项目中引用多个中间件, 中间件依赖有冲突, 使用自定义加载器进行仲裁
+ 修改类加载的方式
+ 扩展加载源
+ `防止源码泄露`

****

**获取当前类的ClassLoader:**

clazz.getClassLoader()

**获取当前线程上下文的ClassLoader:**

Thread.currentThread().getContextClassLoader()

**获取系统过的ClassLoader:**

ClassLoader.getSystemClassLoader()

**获取调用者的ClassLoader:**

DriverManager.getCallerClassLoader()

## 双亲委派机制

![image-20230921174323296](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230921174323296.png)

1. 加载某个类时会先委托父加载器寻找目标类
2. 找不到再 委托上层父加载器加载
3. 如果所有父加载器在自己的加载类路径下都找不到目标类，则在自己的 类加载路径中查找并载入目标类

> 双亲委派机制说简单点就是，先找父亲加载，不行再由儿子自己加载

**为什么要设计双亲委派机制？**

1. 沙箱安全机制：自己写的java.lang.String.class类不会被加载，这样便可以防止核心 API库被随意篡改
2. 避免类的重复加载: 当父加载器已经加载了该类时，就没有必要子ClassLoader再加载一 次，保证被`加载类的唯一性`

**全盘负责委托机制**

`全盘负责`指当一个ClassLoder装载一个类时，除非显示的使用另外一个ClassLoder，该类所依赖及引用的类也由这个ClassLoder载入

## 自定义类加载器

自定义类加载器只需要继承`java.lang.ClassLoader`类，该类有两个核心方法:

+ `loadClass(String, boolean)`，实现了双亲委派机制
+ `findClass`，默认实现是空 方法

自定义类加载器主要是`重写findClass`方法



## 其他

如何判断两个class对象是否是同一个类?

1. 类的完整类名必须一致, 包括包名
2. 类的ClassLoader必须相同

> JVM必须知道一个类是`启动类加载器`加载的, 还是`用户类加载器`加载的
>
> 如果是`用户类加载器`加载的, JVM会将这个`类加载器的一个引用`作为`类型信息`的一部分保存在`方法区`

****

反编译解析二进制class文件:

```shell
javap -v ./xxx.class
```

