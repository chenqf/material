# Deploy

## Mysql

### 单机安装
```shell
export MYSQL_ROOT_PASSWORD=123456
export PORT=3306
export DOCKER_NAME=mysql
mkdir -p /docker/mysql
docker pull mysql:8.0
docker run --name ${DOCKER_NAME} \
-v /docker/mysql/conf:/etc/mysql/conf.d \
-v /docker/mysql/logs:/logs \
-v /docker/mysql/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \
-d -i -p ${PORT}:3306 mysql:8.0
# 进入容器
# docker exec -it ${DOCKER_NAME} /bin/bash
# 开启远程访问
docker exec ${DOCKER_NAME} mysql -uroot -p${MYSQL_ROOT_PASSWORD} \
-e "use mysql; select host,user from user; ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}'; flush privileges;"
```

### 读写分离

TODO 悲观锁是否有问题

## Redis

### 单机安装
```shell
mkdir -p /docker/redis/data
mkdir -p /docker/redis/conf
cd /docker/redis/conf
wget https://raw.githubusercontent.com/redis/redis/7.0/redis.conf
# 指定密码
echo "requirepass chenqfredis" >> redis.conf
# 开启持久化
sed -i 's/appendonly no/appendonly yes/g' redis.conf
# 开启远程访问
sed -i 's/bind 127.0.0.1/#bind 127.0.0.1/g' redis.conf
sed -i 's/protected-mode yes/protected-mode no/g' redis.conf
# 运行 redis
docker run --name redis -v /docker/redis/conf/redis.conf:/etc/redis/redis.conf -v /docker/redis/data:/data -p 6379:6379 -d redis:7.0.0 redis-server /etc/redis/redis.conf
# 访问redis
# docker exec -it redis /bin/bash
# redis-cli -h 127.0.0.1 -p 6379 -a chenqfredis
```

## 线上配置

TODO