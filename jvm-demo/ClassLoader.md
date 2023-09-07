# 类加载器 - Class Loader

![image-20230907103604690](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907103604690.png)

## 类加载过程

1. **加载**
   + 通过类的全限定名获取二进制字节流
   + 将字节流代表的静态存储结构转化为`方法区`的运行时数据结构
   + 内存中生成一个列表这个类的`java.lang.Class对象`, 作为`方法区`中这个类的访问入口
2. **链接**
   + 验证(Verify): 
     + 确保Class文件的字节流信息符合JVM要求, 保证正确性
       + 文件格式验证
       + 元数据验证
       + 字节码验证
       + 符号引用验证
   + 准备(Prepare): 
     + 类的非final静态变量分配内存并设置默认初始值(`零值`)
     + 类的final静态变量, `编译时就会分配内存`, 显示初始化
     + `不会为实例变量分配初始化`, `类变量`分配在`方法区`中, `实例变量`会随对象一起分配到`Java堆`中
   + 解析(Resolve):
     + 将常量池内的符号引用转换为直接引用的过程
3. **初始化**
   + 执行类构造方法\<clinit\>()的过程
     + 类变量赋值和静态代码块中的语句合并而来
     + 执行时按语句在源文件中出现的顺序执行
   + 若具有父类, 子类\<clinit\>()执行前, 父类\<clinit\>()已经执行完毕
   + JVM必须保证\<clinit\>()在多线程下被同步加锁



**以下7中情况才会导致类的初始化:**

1. 创建类实例
2. 访问类或接口的静态变量
3. 调用类的静态方法
4. 反射(比如: Class.forName("com.xxx.xx"))
5. 初始化一个类的子类
6. JVM启动时被标明启动类的类
7. JDK提供的动态语言支持

## 获取ClassLoader

**获取当前类的ClassLoader:**

clazz.getClassLoader()

**获取当前线程上下文的ClassLoader:**

Thread.currentThread().getContextClassLoader()

**获取系统过的ClassLoader:**

ClassLoader.getSystemClassLoader()

**获取调用者的ClassLoader:**

DriverManager.getCallerClassLoader()

## 分类

+ 启动类加载器 Bootstrap Class Loader

1. `C/C++`实现, 嵌套在JVM中
2. 用于加载核心类库
3. 不继承java.lang.ClassLoader, 没有父加载器

+ 扩展类加载器 Extension ClassLoader

1. Java编写
2. 派生于ClassLoader
3. 从java.ext.dirs系统属性的目录中加载类库
4. 从JDK的安装目录下的jre/lib/ext下加载类库

+ 应用程序加载器 App ClassLoader

1. Java编写
2. 派生于ClassLoader
3. 父类加载器是`Extension ClassLoader`
4. 负责加载classpath或java.class.path下的类库
5. 默认的类加载器
6. 如何获取:`ClassLoader.getSystemClassLoader()`

+ 自定义加载器
  + `隔离加载类` eg: 项目中引用多个中间件, 中间件依赖有冲突, 使用自定义加载器进行仲裁
  + 修改类加载的方式
  + 扩展加载源
  + `防止源码泄露`

## 双亲委派机制

 ![image-20230907100502596](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907100502596.png)

1. 收到类加载请求, 将请求委托给父类加载器
2. 父类加载器还存在父类, 依次委托给负载, 直到顶层
3. 若父类加载器能加载, 就成功返回
4. 若父类加载器不能加载, 子加载器尝试自己加载

### 优势

1. 避免重复加载
2. 保护程序安全,防止核心API被篡改

**一个例子:**

![image-20230907101041445](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230907101041445.png)

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

