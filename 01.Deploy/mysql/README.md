# Mysql

## 单机安装
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

## 读写分离

TODO 悲观锁是否有问题