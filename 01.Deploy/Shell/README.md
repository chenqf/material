
```shell
df -h # 显示磁盘空间
```

```shell
# 不输出日志, 后台启动
nohup java -jar log-demo-0.0.1-SNAPSHOT.jar --logging.config=./logback-spring-prod.xml >/dev/null 2>&1 &
```