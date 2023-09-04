# SPI (Service Provider Interface)

一种基于ClassLoader来发现并加载服务的机制

由3个组件构成:

+ Service
+ Service Provider
+ ServiceLoader

**Service:** 一个公开的接口或抽象类, 定义了一个抽象的功能模块

**Service Provider:** 是Service接口类的一个实现类

**ServiceLoader:** SPI中的核心组件, 负载在运行时发现并加载`Service Provider`

![image-20230905091833100](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230905091833100.png)

## 规范实现

1. 规范的配置文件
   + **文件路径:** 必须在Jar包中的`META-INF/services`目录下
   + **文件名称:**Service接口的全限定名
   + **文件内容:**Service实现类的全限定名, 多个实现类则每一个实现类在文件中单独一行

2. `Service Provider`类必须具备无参的默认构造方法
   + 因为通过反射技术实例化它时, 时不带参数的.
3. 能够加载到配置文件和`Service Provider`
   + **方式一:** 将`Service Provider`的`Jar`包放到`Classpath`中(最常用)
   + **方式二:** 将`Jar`包安装到`Jre`的扩展目录下
   + **方式三:** 自定义一个`ClassLoader`

## Spring-Boot 自动配置

![image-20230905095835874](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230905095835874.png)

现有`spi-demo` 和 `common`项目, 希望`spi-demo`引入`common`后, 自动对`common`中的`Bean`进行注册

**spi-demo:**

```xml
<dependency>
    <groupId>com.maple</groupId>
    <artifactId>common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**common:**

![image-20230905103639021](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230905103639021.png)