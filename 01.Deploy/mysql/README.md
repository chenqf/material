# Mysql

```shell
docker network create maple-net
docker pull mysql:8.0
```

## 单机安装

```shell
export MYSQL_ROOT_PASSWORD=123456
export PORT=3306
export DOCKER_NAME=mysql-standalone
export BASE_DIR=/docker/mysql/standalone
mkdir -p ${BASE_DIR}
docker stop ${DOCKER_NAME} &> /dev/null
docker rm ${DOCKER_NAME} &> /dev/null
docker run --name ${DOCKER_NAME} --network maple-net -v ${BASE_DIR}/conf:/etc/mysql/conf.d -v ${BASE_DIR}/logs:/logs \
-v ${BASE_DIR}/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} -d -i -p ${PORT}:3306 mysql:8.0
```

开启远程访问:

```shell
# 进入mysql控制台
docker exec -it ${DOCKER_NAME} mysql -uroot -p${MYSQL_ROOT_PASSWORD} 
```

```sql
use mysql;
select host, user from user;
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456'; 
flush privileges;

# 8+适用, 但需要本地换客户端
# create user root@'%' identified by '123456';
# grant all privileges on *.* to root@'%' with grant option;
# flush privileges;
```

## 主从集群

```shell
export MYSQL_ROOT_PASSWORD=123456
export BASE_DIR=/docker/mysql/master-slave

rm -rf ${BASE_DIR}/master
rm -rf ${BASE_DIR}/slave
mkdir -p ${BASE_DIR}/master
mkdir -p ${BASE_DIR}/slave

docker network create maple-net
docker stop mysql-master &> /dev/null
docker rm mysql-master &> /dev/null
docker stop mysql-slave &> /dev/null
docker rm mysql-slave &> /dev/null

docker run -d --name mysql-master --network maple-net -p 3307:3306 \
-v ${BASE_DIR}/master/conf:/etc/mysql/conf.d -v ${BASE_DIR}/master/logs:/logs -v ${BASE_DIR}/master/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} mysql:8.0 \
--server-id=1 --log_bin=master-bin --log_bin-index=master-bin.index \
--default_authentication_plugin=mysql_native_password

docker run -d --name mysql-slave --network maple-net -p 3308:3306 \
-v ${BASE_DIR}/slave/conf:/etc/mysql/conf.d -v ${BASE_DIR}/slave/logs:/logs -v ${BASE_DIR}/slave/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} mysql:8.0 \
--server-id=2 --log_bin=mysql-bin --relay-log-index=slave-relay-bin.index --relay-log=slave-relay-bin --read-only=1 \
--default_authentication_plugin=mysql_native_password 

```

**开启主从mysql的远程连接**

```shell
docker exec -it mysql-master mysql -uroot -p -e "use mysql;select host, user from user;ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';flush privileges;"
docker exec -it mysql-slave mysql -uroot -p -e "use mysql;select host, user from user;ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';flush privileges;"
```

**给master分配个replication slave的权限**

```shell
docker exec -it mysql-master mysql -uroot -p -e "GRANT REPLICATION SLAVE ON *.* TO 'root'@'%';flush privileges;"
docker exec -it mysql-master mysql -uroot -p -e "show master status;"
```

**记录上一步输出的File和Position, 并设置主从同步**

```shell
# 进入mysql-slave
docker exec -it mysql-slave mysql -uroot -p
```

```shell
# 注意，CHANGE MASTER指令中需要指定的MASTER_LOG_FILE和MASTER_LOG_POS必须与主服务中查到的保持一致
CHANGE MASTER TO
MASTER_HOST='mysql-master',
MASTER_PORT=3306,
MASTER_USER='root',
MASTER_PASSWORD='123456',
MASTER_LOG_FILE='master-bin.000003',
MASTER_LOG_POS=3382,
GET_MASTER_PUBLIC_KEY=1;
#开启slave
start slave;
#查看主从同步状态
show slave status; # Slave_IO_Running Yes / Slave_SQL_Running Yes   :代表同步成功
```

**检查是否同步一致**: 过检查主服务与从服务之间的File和Position这两个属性是否一致来确定

若出现同步不一致, 可按上面步骤重新同步

### 半同步复制集群

TODO: 什么是半同步复制集群

再原有基础上, 执行一下操作:

```shell
# 登陆主服务
docker exec -it mysql-master mysql -uroot -p
```

```shell
# 通过扩展库来安装半同步复制模块
install plugin rpl_semi_sync_master soname 'semisync_master.so';
# 查看系统全局参数，rpl_semi_sync_master_timeout就是半同步复制时等待应答的最长等待时间
show global variables like 'rpl_semi%';
# 是打开半同步复制的开关。
set global rpl_semi_sync_master_enabled=ON;
exit
```

```shell
# 登陆从服务
docker exec -it mysql-slave mysql -uroot -p
```

```shell
# 通过扩展库来安装半同步复制模块
install plugin rpl_semi_sync_slave soname 'semisync_slave.so';
# 是打开半同步复制的开关。
set global rpl_semi_sync_slave_enabled = on;
# 查看系统全局参数
show global variables like 'rpl_semi%';
# 重启slave服务
stop slave;
start slave;
exit
```


### 扩容与数据迁移

是如果我们的集群是已经运行过一段时间，这时候如果要扩展新的从节点就有一个问题，之前的数据没办法从binlog来恢复了

```shell
# 进入master容器
docker exec -it mysql-master /bin/bash
# 备份所有数据库
mysqldump -uroot -p  --all-databases > /backup.sql
exit
# 将备份文件copy到slave
docker cp mysql-master:/backup.sql ./
docker cp ./backup.sql mysql-slave:/
# 进入slave容器
docker exec -it mysql-slave /bin/bash
# 将数据导入slave
mysql -uroot -p < /backup.sql
exit
```

导入数据后, 再进行主从同步操作即可