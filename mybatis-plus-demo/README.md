# MyBatis-Plus


## 常用注解

+ @TableName  表名注解，标识实体类对应的表
+ @TableId    指定主键
+ @TableField 指定表中的字段 / 处理`公共字段填充`
+ @TableLogic 指定逻辑删除字段, 设置后无力删除变更为逻辑删除
+ @Version    指定乐观锁配置


### 实体类配置
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User {
    @TableId(value = "id",type = IdType.AUTO) // 默认为雪花算法, auto代表数据库自增id
    private Long id;
    private String name;
    private Integer age;
    @TableField("email")
    private String email;
    @TableLogic
    private Integer isDelete;
    @Version
    private Integer version;
}
```


## 常用插件

+ 分页插件
+ 乐观锁插件
+ 逻辑多租户隔离插件
  + 业务表增加租户字段, 多个租户数据在同一张表中, 通过租户字段进行隔离
  + 须放在分页插件之前
+ 防全表更新与删除插件
+ 动态表名插件
  + 可实现表级多租户隔离

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 逻辑租户隔离
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
           @Override
           public Expression getTenantId() {
              // 通过request或其他方式,获取租户ID
              // HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
              return new LongValue(1);
           }
           @Override
           public boolean ignoreTable(String tableName) {
              // 当前表是否要进行租户过滤
              return "user".equalsIgnoreCase(tableName);
           }
           @Override
           public String getTenantIdColumn() {
              // 租户字段名
              return "tenant_id";
           }
        }));
        //添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        //添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        //防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        //动态表名 - 对XML中的sql也生效
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
           // 通过request或其他方式,获取表名
           HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
           String dynamicTableSuffix = request.getParameter("suffix");
           return tableName + (dynamicTableSuffix == null ? "" : dynamicTableSuffix); // 返回真实表名
        });
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }
}
```

> 逻辑多租户 + 动态表名, 存在冲突问题, sql中TenantId的追加指向的还是原表名, 2者同时使用时使用其他方案

> 更新数据时,若使用UpdateWrapper并且需要乐观锁功能, 必须将实例类实例传入update方法

### 分页

**普通使用分插件**

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Override
    public void testPage() {
        Page<User> page = new Page<>(1, 2); // 第一页, 每页2条
        userMapper.selectPage(page, null);
        List<User> list = page.getRecords(); // 查询到的实体数据列表
        list.forEach(System.out::println);
        System.out.println("当前页："+page.getCurrent());
        System.out.println("每页显示的条数："+page.getSize());
        System.out.println("总记录数："+page.getTotal());
        System.out.println("总页数："+page.getPages());
        System.out.println("是否有上一页："+page.hasPrevious());
        System.out.println("是否有下一页："+page.hasNext());
    }
}
```

**XML方式使用分页插件**

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    Page<User> selectPageVo(@Param("page") Page<User> page, @Param("age") Integer age);
}
```
```xml
<mapper namespace="com.maple.mp.mapper.UserMapper">
   <select id="selectPageVo" resultType="User">
       select * from user where age = #{age}
   </select>
</mapper>
```
```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void selectPageVo() {
        Page<User> page = new Page<>(1, 2); // 第一页, 每页2条
        userMapper.selectPageVo(page, 30);
        List<User> list = page.getRecords(); // 查询到的实体数据列表
        list.forEach(System.out::println);
        System.out.println("当前页："+page.getCurrent());
        System.out.println("每页显示的条数："+page.getSize());
        System.out.println("总记录数："+page.getTotal());
        System.out.println("总页数："+page.getPages());
        System.out.println("是否有上一页："+page.hasPrevious());
        System.out.println("是否有下一页："+page.hasNext());
    }
}
```

### 动态表名

```java

```

## 公共字段自动填充

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {
    @TableField(fill = FieldFill.INSERT) // 插入时处理
    private String createBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时处理
    private String updateBy;
    @TableField(fill = FieldFill.INSERT) // 插入时处理
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时处理
    private Date updateTime;
}
```

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User extends BaseEntity {
    //....
}
```

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "createBy", String.class, getUserName());
        this.strictInsertFill(metaObject, "updateBy", String.class, getUserName());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateBy", String.class, getUserName());
    }

    /**
     * 从token或者redis中动态解析
     */
    private String getUserName(){
        // 获取HttpServletRequest对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // 在这里可以根据需要获取请求中的一些信息，并进行相应的填充操作
        System.out.println("MyMetaObjectHandler" + request.getRequestURI());
        return "admin";
    }
}
```

## 枚举字段

使用`@EnumValue`标识数据存储时使用对应字段值

```java
@Getter
@AllArgsConstructor
public enum SexEnum{
    MALE(1, "男"),
    FEMALE(2,"女"),
    UNKNOWN(3,"未知");

    @EnumValue
    private final int value;
    private final String desc;

    @Override
    public String toString() {
        return this.desc;
    }
}
```
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User extends BaseEntity {
    // ...
    private SexEnum sex; // 使用枚举作为类型
}
```

## 逻辑隔离多租户

TODO 

1. 数据库存储租户与之对应的数据库
2. 代码中根据数据库中的数据创建多个数据源
3. 定义@Tenant+AOP, 动态获取tenantId,并指定当前数据源

## 冗余字段处理

## 表字段加解密


水平拆分 & 垂直拆分

多租户, 保护防止误操作
 表自动创建: spring.sql.init.schema-locations
多表查询, 如何返回对象

