
## 系统命令

```shell
systemctl start docker
systemctl stop docker
systemctl restart docker
systemctl status docker # show status
systemctl enable docker # start computer auto start

docker info # show main info

docker system df # 查看image/container/data 所占用的空间
```

## 镜像相关

```shell
docker images # show all image for local computer
docker search <i-name> # search image
docker pull <i-name> # download image
docker rmi [-f] <i-name> # delete image -f[force]
```

基于当前容器生成镜像:

```shell
# 本地生成新镜像
# docker commit -m="提交的描述信息" -a="作者" <c-id> <registry-name>:<tag>
docker commit -m="add vim ubuntu" -a="chenqf" 1b9d0694278b maple/mybuntun:1.0
```

上传镜像到阿里云镜像服务:

```shell
# namespace: 区分项目和环境
# registry: 仓库名,代表哪个程序
docker login --username=chenqf881006 registry.cn-hangzhou.aliyuncs.com # cqf13002173@
docker tag [ImageId] registry.cn-hangzhou.aliyuncs.com/<namespace>/<registry>:[镜像版本号]
docker push registry.cn-hangzhou.aliyuncs.com/<namespace>/<registry>:[镜像版本号]
```

从阿里云镜像服务拉取镜像:

```shell
docker login --username=chenqf881006 registry.cn-hangzhou.aliyuncs.com # cqf13002173@
docker pull registry.cn-hangzhou.aliyuncs.com/<namespace>/<registry>:[镜像版本号]
```

### DockFile

1. 编写Dockerfile文件
2. docker build 构建镜像
3. docker run 运行容器

+ FROM : 基于哪个镜像, 一般都是第一条
+ RUN  : 容器构建时需要运行的命令
  + RUN <命令行命令> 
  + RUN ["<可执行文件>", "<ARG1>","<ARG2>"]
+ EXPOSE : 当前容器暴露的接口
+ WORKDIR : 指定创建容器后, 终端登陆进入的工作目录
+ USER : 指定该镜像以什么用户去执行, 默认是root
+ ENV : 设置环境变量
  + ENV MY_VAL=test
+ ADD : 将主机目录下的文件COPY到镜像中, 且会自动处理URL和解压tar压缩包
+ COPY
+ VOLUME : 容器卷
+ CMD : 指定容器启动后要做的事
  + 可以有多个, 但只有最后一个CMD生效
  + 若docker run后跟其他参数, 最后一个CMD会被覆盖
+ ENTRYPOINT: 可以和CMD一起使用, 这里CMD等于在给ENTRYPOINT传参

**示例:**

```dockerfile
FROM ubuntu:23.10
MAINTAINER chenqifeng<546710115@qq.com>

RUN apt-get update
RUN apt-get -y install wget
RUN wget https://repo.huaweicloud.com/java/jdk/8u202-b08/jdk-8u202-linux-x64.tar.gz
RUN mkdir /usr/local/java/
RUN tar -zxvf jdk-8u202-linux-x64.tar.gz -C /usr/local/java
RUN rm jdk-8u202-linux-x64.tar.gz

ENV JAVA_HOME /usr/local/java/jdk1.8.0_202
ENV JRE_HOME $JAVA_HOME/jre
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib:$CLASSPATH
ENV PATH $JAVA_HOME/bin:$PATH
```


**基于Dockerfile构建镜像:**

```shell
# 须指定Dockerfile文件所在文件夹
docker build -t java8env:0.1 <Dockerfile-Dir>
```

```dockerfile
FROM java8env:0.1
ADD kafka-demo-0.0.1-SNAPSHOT.jar chenqf.jar
ENTRYPOINT ["java","-jar","/chenqf.jar"]
EXPOSE 8015
```

#### 私有库 TODO harbor

创建私有库:

```shell
export REGISTRY_DIR=/docker/registry
docker pull registry
mkdir -p REGISTRY_DIR
docker run -d -p5000:5000 -v ${REGISTRY_DIR}:/tmp/registry --privileged=true registry
```

查看私服库中的镜像:
```shell
curl -XGET http://127.0.0.1:5000/v2/_catalog
```

基于当前容器生成镜像:
```shell
# docker commit -m="提交的描述信息" -a="作者" <c-id> <registry-name>:<tag>
docker commit -m="add vim ubuntu" -a="chenqf" 1b9d0694278b maple/mybuntun:1.0
```

修改镜像为符合私服规范的Tag
```shell
docker tag <new-i-id> 127.0.0.1:5000/<namespace>/<registry>:<version>
```

修改本地docker配置, 将私服库加入安全队列

```shell
vim /etc/docker/

#{
#  "registry-mirrors": ["https://5k086x1v.mirror.aliyuncs.com"],
#  "insecure-registries": ["127.0.0.1:5000"]
#}
# 若无效, 须重启docker
```

上传新镜像到私服库
```shell
docker push 127.0.0.1:5000/<namespace>/<registry>:<version>
```

#### 虚悬镜像

构建镜像时出现错误, 仓库名和标签都是None, 实际没有用还占用空间, 需要删掉

## 容器

**docker run [OPTIONS] IMAGE [COMMAND] [ARG...]**

+ --name 容器名字
+ -d 启动守护式容器
+ -it 启动交互式容器
+ -P 随机端口映射
+ -p 指定端口映射 -p[主机端口]:[容器端口]
+ -v 指定数据卷 
+ --privileged 扩大容器的权限, 使用挂载卷一定要使用

```shell
docker run -it <c-name> /bin/bash # 以交互模式启动容器
docker exec -it <c-name> /bin/bash # 新进程进入容器 exit不导致容器退出
docker attach <c-name> /bin/bash # 直接进入容器终端, 不启动新进程 exit导致容器退出
docker inspect <c-name> # show container detail info
docker cp <local-file-path> <c-id>:<c-path> # copy local file to container 
docker cp <c-id>:<c-path> <local-file-path>  # copy container file to local
docker export <c-id> > file.tar # export all container to local
docker import <文件名>.tar | docker import - <镜像用户>/<镜像名>:<镜像版本号> # 将export到处的内容重新生成容器
```
















