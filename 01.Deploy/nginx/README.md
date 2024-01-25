# Nginx

## 最简安装

通过官方镜像获取配置文件, copy后再进行挂载安装

```shell
export BASE_DIR=/opt/docker-data/nginx
export NAME=nginx
export VERSION=1.20
docker run --name ${NAME} -p 80:80 -d nginx:${VERSION};
mkdir -p ${BASE_DIR}/logs;
docker cp ${NAME}:/etc/nginx/conf.d/ ${BASE_DIR};
docker cp ${NAME}:/usr/share/nginx/html/ ${BASE_DIR};
docker cp ${NAME}:/etc/nginx/nginx.conf ${BASE_DIR};
docker stop ${NAME};
docker rm ${NAME};
docker run --name nginx -p 80:80 \
-v ${BASE_DIR}/conf.d/:/etc/nginx/conf.d/ \
-v ${BASE_DIR}/nginx.conf:/etc/nginx/nginx.conf \
-v ${BASE_DIR}/html:/usr/share/nginx/html/ \
-v ${BASE_DIR}/logs/:/var/log/nginx/ \
--privileged=true -d nginx:${VERSION};
```


Nginx官方镜像不带Stream模块, 通过Dockerfile创建自定义Nginx镜像

### 准备Nginx配置文件 - nginx.conf

```shell
worker_processes  1;
events {
    worker_connections  1024;
}
stream {
    include  /usr/local/nginx/stream/*.conf;
}
http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  65;
    include /usr/local/nginx/conf.d/*.conf;
    server {
        listen       80;
        server_name  localhost;
        location / {
            root   html;
            index  index.html index.htm;
        }
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```



### Dockerfile

```dockerfile
FROM centos:centos7
MAINTAINER chenqifeng<546710115@qq.com>
RUN yum -y install gcc gcc-c++ autoconf automake
RUN yum -y install zlib zlib-devel openssl openssl-devel pcre-devel
WORKDIR /usr/src/nginx
RUN curl -O https://nginx.org/download/nginx-1.25.2.tar.gz
RUN tar -zxvf nginx-1.25.2.tar.gz
WORKDIR /usr/src/nginx/nginx-1.25.2
RUN ./configure --with-stream && make && make install
COPY ./nginx.conf /usr/local/nginx/conf
RUN rm -rf /usr/src/nginx
WORKDIR /usr/local/nginx
CMD ["/usr/local/nginx/sbin/nginx", "-g", "daemon off;"]
```

### 构建镜像
RUN rm nginx-1.25.2.tar.gz && rm -rf rm nginx-1.25.2

```shell
docker build -t nginx-stream:1.25.2 .
```

### 启动nginx

```shell
docker run -d -p 80:80 --name nginx-stream nginx-stream:1.25.2
```
