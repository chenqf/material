# zookeeper

## 单机

tickTime  集群间的心跳时间
initLimit 初始周期,几个心跳时间, 数据同步的周期时间(周期时间同步失败,认为挂掉了
syncLimit 发送请求和心跳的周期(几个心跳时间), 超过时间,认为挂掉了

```shell
export DOCKER_NAME=zookeeper
export DATA_DIR=/docker/zookeeper/standalone/data
export CONF_DIR=/docker/zookeeper/standalone/conf
export VERSION=3.7.1
export USERNAME=chenqf
export PASSWORD=123456
export PORT=2181
mkdir -p ${DATA_DIR}
mkdir -p ${CONF_DIR}
rm -rf ${CONF_DIR}/*
# zoo.cfg
echo "# 集群间的心跳时间" >> ${CONF_DIR}/zoo.cfg
echo "tickTime=2000 " >> ${CONF_DIR}/zoo.cfg
echo "# 初始周期,几个心跳时间, 数据同步的周期时间(周期时间同步失败,认为挂掉了)" >> ${CONF_DIR}/zoo.cfg
echo "initLimit=10" >> ${CONF_DIR}/zoo.cfg
echo "# 发送请求和心跳的周期(几个心跳时间), 超过时间,认为挂掉了" >> ${CONF_DIR}/zoo.cfg
echo "syncLimit=20" >> ${CONF_DIR}/zoo.cfg
echo "# 数据目录" >> ${CONF_DIR}/zoo.cfg
echo "dataDir=/data" >> ${CONF_DIR}/zoo.cfg
echo "# 暴露的端口号" >> ${CONF_DIR}/zoo.cfg
echo "clientPort=${PORT}" >> ${CONF_DIR}/zoo.cfg
echo "# 客户端连接的最大连接数" >> ${CONF_DIR}/zoo.cfg
echo "maxClientCnxns=100" >> ${CONF_DIR}/zoo.cfg
echo "authProvider.1=org.apache.zookeeper.server.auth.SASLAuthenticationProvider" >> ${CONF_DIR}/zoo.cfg
echo "requireClientAuthScheme=sasl" >> ${CONF_DIR}/zoo.cfg
# zoo_jaas.conf
echo "Server {" >> ${CONF_DIR}/zoo_jaas.conf
echo "  org.apache.zookeeper.server.auth.DigestLoginModule required" >> ${CONF_DIR}/zoo_jaas.conf
echo "  user_super=\"${USERNAME}\" pass_super=\"${PASSWORD}\"" >> ${CONF_DIR}/zoo_jaas.conf
echo "  user_reader=\"${USERNAME}\" pass_reader=\"${PASSWORD}\";" >> ${CONF_DIR}/zoo_jaas.conf
echo "};" >> ${CONF_DIR}/zoo_jaas.conf
# zoo.env
echo "ZOOKEEPER_USER=${USERNAME}" >> ${CONF_DIR}/zoo.env
echo "ZOOKEEPER_PASSWORD=${PASSWORD}" >> ${CONF_DIR}/zoo.env
docker pull zookeeper:${VERSION}
docker stop ${DOCKER_NAME} &> /dev/null
docker rm ${DOCKER_NAME} &> /dev/null
docker run --name ${DOCKER_NAME} --restart always -p ${PORT}:${PORT} -d \
-v ${CONF_DIR}/zoo.env:/env/zookeeper.env \
-v ${CONF_DIR}/zoo_jaas.conf:/conf/zoo_jaas.conf \
-v ${CONF_DIR}/zoo.cfg:/conf/zoo.cfg zookeeper:${VERSION}

# docker exec -it ${DOCKER_NAME} ./bin/zkCli.sh -Djava.security.auth.login.config=/conf/zoo_jaas.conf
```