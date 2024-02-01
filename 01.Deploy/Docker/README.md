# Docker

## ubuntu安装

https://docs.docker.com/engine/install/ubuntu/



## 镜像加速 (容器镜像服务)

```shell
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://5k086x1v.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

