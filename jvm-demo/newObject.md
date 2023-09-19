# 创建对象

## 创建对象的方式

1. new
2. Class的newInstance() : JDK9已过时
3. Constructor的newInstance(xx) : JDK9+
4. 使用clone()
5. 使用反序列化

## 对象内存布局

![image-20230915093534048](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230915093534048.png)

```java
public class NewObj {
    public static void main(String[] args) {
        Customer customer = new Customer();
    }
}

class Customer{
    private long id = 100;
    private String name;
    private Account acct;
    {
        name = "匿名客户";
    }
    public Customer(){
        acct = new Account();
    }

}

class Account{}
```

![image-20230915094049710](https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/images/image-20230915094049710.png)