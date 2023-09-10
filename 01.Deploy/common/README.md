

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
```

```shell
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
```

```shell
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