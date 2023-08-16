
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

仓库名和标签都是None, 实际没有用, 可以删掉

## 容器

**docker run [OPTIONS] IMAGE [COMMAND] [ARG...]**

+ --name 容器名字
+ -d 启动守护式容器
+ -it 启动交互式容器
+ -P 随机端口映射
+ -p 指定端口映射 -p[主机端口]:[容器端口]
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
















