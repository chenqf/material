# elastic-search

## 单机部署

```shell
docker network create search-net
```

```shell
docker run -d --name elasticsearch --net search-net -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:7.17.10
```