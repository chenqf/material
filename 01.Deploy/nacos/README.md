# Nacos

1. 部署3台以上nacos作为一个集群
2. 将nacos连接到mysql做数据持久化
3. 使用nginx做负载均衡器


### 定义所有变量

```shell
export NET_NAME=maple-net
export MYSQL_VERSION=8.0 # 5.7 # 
export MYSQL_DOCKER_NAME=mysql-nacos
export MYSQL_SERVICE_HOST=$MYSQL_DOCKER_NAME
export MYSQL_SERVICE_USER=root
export MYSQL_ROOT_PASSWORD=123456
export MYSQL_SERVICE_DB_NAME=nacos
export NACOS_DOCKER_NAME_PREFIX=nacos
export MYSQL_BASE_DIR=/docker/mysql/nacos
export NACOS_SQL_URL=https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/config/nacos-2.2.0-mysql-schema.sql
export NACOS_VERSION=v2.2.0
export NACOS_SERVER_NUM=3
export NACOS_AUTH_IDENTITY_KEY=3c6e0b8a9c15224a8228b9a98ca1531d
export NACOS_AUTH_IDENTITY_VALUE=2063c1608d6e0baf80249c42e2be5804
export NACOS_AUTH_TOKEN=94a08da1fecbb6e8b46990538c7b50b2
export NGINX_VERSION=1.25
export NGINX_DOCKER_NAME=nginx-nacos
export NGINX_BASE_DIR=/docker/nginx/nacos
export NGINX_NACOS_HTTP_SERVER=nginx-nacos-server
export NGINX_NACOS_STREAM_SERVER=nginx-nacos-grpc
```

### 定义通用函数

```shell
# 根据入参下载镜像及对应版本
check_and_download_docker_image() {
  local image_name="$1"
  local image_version="$2"

  # 检查镜像是否存在
  if docker image inspect "$image_name:$image_version" &> /dev/null; then
    echo "镜像 $image_name:$image_version 已存在。"
  else
    echo "镜像 $image_name:$image_version 不存在，正在下载..."
    docker pull "$image_name:$image_version"
    if [ $? -eq 0 ]; then
      echo "镜像 $image_name:$image_version 下载成功。"
    else
      echo "下载镜像 $image_name:$image_version 失败。"
      return 1
    fi
  fi
}
# 根据入参创建Docker网络
check_and_create_docker_network() {
  local network_name="$1"

  # 检查网络是否存在
  if docker network inspect "$network_name" &> /dev/null; then
    echo "网络 $network_name 已存在。"
  else
    echo "网络 $network_name 不存在，正在创建..."
    docker network create "$network_name"
    if [ $? -eq 0 ]; then
      echo "网络 $network_name 创建成功。"
    else
      echo "创建网络 $network_name 失败。"
      return 1
    fi
  fi
}
# 根据入参删除容器
check_and_delete_docker_container() {
  local container_name="$1"

  # 检查容器是否存在
  if docker ps -a --filter "name=$container_name" --format "{{.Names}}" | grep -q "$container_name"; then
    # 检查容器状态
    if docker ps -q --filter "name=$container_name"; then
      # 容器正在运行，停止它
      echo "容器 $container_name 正在运行，正在停止..."
      docker stop "$container_name"
    fi

    # 删除容器
    echo "删除容器 $container_name..."
    docker rm "$container_name"
    echo "容器 $container_name 已删除。"
  else
    echo "容器 $container_name 不存在。"
  fi
}
```

### 下载镜像 & 创建网络

```shell
check_and_download_docker_image mysql $MYSQL_VERSION
check_and_download_docker_image nacos/nacos-server $NACOS_VERSION
check_and_create_docker_network $NET_NAME
```

### 启动用于Nacos的Mysql

```shell
check_and_delete_docker_container $MYSQL_DOCKER_NAME
mkdir -p $MYSQL_BASE_DIR
docker run --name $MYSQL_DOCKER_NAME \
--network $NET_NAME --hostname=$MYSQL_DOCKER_NAME --restart=always \
-v $MYSQL_BASE_DIR/conf:/etc/mysql/conf.d \
-v $MYSQL_BASE_DIR/logs:/logs \
-v $MYSQL_BASE_DIR/data:/var/lib/mysql \
-e MYSQL_DATABASE=$MYSQL_SERVICE_DB_NAME \
-e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD -d -i  mysql:$MYSQL_VERSION

SQL_USER="use mysql; select host,user from user; ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}'; flush privileges;"
SQL_NACOS_DB="DROP DATABASE IF EXISTS ${MYSQL_SERVICE_DB_NAME};CREATE DATABASE IF NOT EXISTS ${MYSQL_SERVICE_DB_NAME} DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;"
SQL_NACOS_TABLE=`curl -s $NACOS_SQL_URL`
SQL_NACOS_TABLE="use $MYSQL_SERVICE_DB_NAME;$SQL_NACOS_TABLE"
docker exec -it $MYSQL_DOCKER_NAME mysql -uroot -p -e "$SQL_USER$SQL_NACOS_DB$SQL_NACOS_TABLE" 
```

### 启动Nacos

```shell
MODE=standalone
NACOS_SERVERS=""
NACOS_AUTH_TOKEN_MD5=$(echo -n $NACOS_AUTH_TOKEN | md5sum | awk '{print $1}')
if [ $NACOS_SERVER_NUM -gt 1 ]; then
  MODE=cluster
fi

for ((j=1; j<=$NACOS_SERVER_NUM; j++)); do
  NACOS_SERVERS="$NACOS_SERVERS $NACOS_DOCKER_NAME_PREFIX$j:8848"
done

NACOS_AUTH_IDENTITY_KEY_MD5=$(echo -n $NACOS_AUTH_IDENTITY_KEY | base64)
NACOS_AUTH_IDENTITY_VALUE_MD5=$(echo -n $NACOS_AUTH_IDENTITY_VALUE | base64)
NACOS_AUTH_TOKEN_MD5=$(echo -n $NACOS_AUTH_TOKEN | base64)
for ((j=1; j<=$NACOS_SERVER_NUM; j++)); do
    check_and_delete_docker_container $NACOS_DOCKER_NAME_PREFIX$j
    docker run --name $NACOS_DOCKER_NAME_PREFIX$j --restart=always \
    --hostname=$NACOS_DOCKER_NAME_PREFIX$j \
    --network $NET_NAME \
    -e MODE=$MODE \
    -e PREFER_HOST_MODE=hostname \
    -e NACOS_SERVERS="$NACOS_SERVERS" \
    -e SPRING_DATASOURCE_PLATFORM=mysql \
    -e MYSQL_SERVICE_HOST=$MYSQL_SERVICE_HOST \
    -e MYSQL_SERVICE_PORT=$MYSQL_SERVICE_PORT \
    -e MYSQL_SERVICE_DB_NAME=$MYSQL_SERVICE_DB_NAME \
    -e MYSQL_SERVICE_USER=$MYSQL_SERVICE_USER \
    -e MYSQL_SERVICE_PASSWORD=$MYSQL_ROOT_PASSWORD \
    -e NACOS_AUTH_IDENTITY_KEY=$NACOS_AUTH_IDENTITY_KEY_MD5 \
    -e NACOS_AUTH_IDENTITY_VALUE=$NACOS_AUTH_IDENTITY_VALUE_MD5 \
    -e NACOS_AUTH_TOKEN=$NACOS_AUTH_TOKEN_MD5 \
    -e NACOS_AUTH_ENABLE=true \
    -d nacos/nacos-server:$NACOS_VERSION
done
```


### 启动Nginx做负载均衡

官方Nginx镜像不包含Stream模块, 所以单独构建一个本地镜像nginx-stream

TODO Dockerfile

```shell
rm -rf $NGINX_BASE_DIR/conf.d
rm -rf $NGINX_BASE_DIR/stream
mkdir -p $NGINX_BASE_DIR/conf.d
mkdir -p $NGINX_BASE_DIR/stream

echo "" > $NGINX_BASE_DIR/conf.d/nacos.conf
echo "upstream ${NGINX_NACOS_HTTP_SERVER} {" >> $NGINX_BASE_DIR/conf.d/nacos.conf
echo "}" >> $NGINX_BASE_DIR/conf.d/nacos.conf
echo "server {" >> $NGINX_BASE_DIR/conf.d/nacos.conf
echo "    listen 8848;" >> $NGINX_BASE_DIR/conf.d/nacos.conf
echo "    location / {" >> $NGINX_BASE_DIR/conf.d/nacos.conf
echo "      proxy_pass http://${NGINX_NACOS_HTTP_SERVER}/;" >> $NGINX_BASE_DIR/conf.d/nacos.conf
echo "    }" >> $NGINX_BASE_DIR/conf.d/nacos.conf
echo "}" >> $NGINX_BASE_DIR/conf.d/nacos.conf

for ((j=1; j<=$NACOS_SERVER_NUM; j++)); do
  NEW_LINE="      server $NACOS_DOCKER_NAME_PREFIX$j:8848;"
  sed -i "/upstream $NGINX_NACOS_HTTP_SERVER/ a\\$NEW_LINE" $NGINX_BASE_DIR/conf.d/nacos.conf
done

echo "" > $NGINX_BASE_DIR/stream/nacos.conf
echo "upstream ${NGINX_NACOS_STREAM_SERVER} {" >> $NGINX_BASE_DIR/stream/nacos.conf
echo "}" >> $NGINX_BASE_DIR/stream/nacos.conf
echo "server {" >> $NGINX_BASE_DIR/stream/nacos.conf
echo "    listen 9848;" >> $NGINX_BASE_DIR/stream/nacos.conf
echo "    proxy_pass ${NGINX_NACOS_STREAM_SERVER};" >> $NGINX_BASE_DIR/stream/nacos.conf
echo "}" >> $NGINX_BASE_DIR/stream/nacos.conf
for ((j=1; j<=$NACOS_SERVER_NUM; j++)); do
  NEW_LINE="  server $NACOS_DOCKER_NAME_PREFIX$j:9848;"
  sed -i "/upstream $NGINX_NACOS_STREAM_SERVER/ a\\$NEW_LINE" $NGINX_BASE_DIR/stream/nacos.conf
done

check_and_delete_docker_container $NGINX_DOCKER_NAME
docker run -d -p 8848:8848 --network $NET_NAME \
-v $NGINX_BASE_DIR/conf.d:/usr/local/nginx/conf.d/ \
-v $NGINX_BASE_DIR/stream:/usr/local/nginx/stream/ \
--name $NGINX_DOCKER_NAME nginx-stream:1.25.2
```





