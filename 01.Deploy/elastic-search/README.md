# elastic-search

## 单机部署

```shell
docker network create search-net
```

```shell
docker stop elasticsearch &> /dev/null
docker rm elasticsearch &> /dev/null
export DATA_DIR=/docker/standalone/data
export LOGS_DIR=/docker/standalone/logs
export CONF_DIR=/docker/standalone/config
export PLUG_DIR=/docker/standalone/plugin
mkdir -p ${DATA_DIR}
mkdir -p ${LOGS_DIR}
mkdir -p ${CONF_DIR}
mkdir -p ${PLUG_DIR}
rm -rf ${CONF_DIR}/*
echo 'cluster.name: "docker-elastic"' >> ${CONF_DIR}/elasticsearch.yml
echo "network.host: 0.0.0.0" >> ${CONF_DIR}/elasticsearch.yml
echo "discovery.type: single-node" >> ${CONF_DIR}/elasticsearch.yml
#echo "path.data: /data" >> ${CONF_DIR}/elasticsearch.yml
#echo "path.logs: /logs" >> ${CONF_DIR}/elasticsearch.yml
#echo "bootstrap.memory_lock: false" >> ${CONF_DIR}/elasticsearch.yml
docker run -d --name elasticsearch --net search-net --link elasticsearch:elasticsearch \
-p 9200:9200 -v ${PLUG_DIR}:/usr/share/elasticsearch/plugins -v ${DATA_DIR}:/data -v ${LOGS_DIR}:/logs \
-v ${CONF_DIR}/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml elasticsearch:7.17.3

# 安装icu分词插件
#docker exec -it elasticsearch bin/elasticsearch-plugin install analysis-icu
# 安装ik分词插件
#docker exec -it elasticsearch bin/elasticsearch-plugin install https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/sortware/elasticsearch-analysis-ik-7.17.3.zip

docker restart elasticsearch
# docker exec -it elasticsearch /bin/bash
# curl --location --request PUT 'http://127.0.0.1:9200/es_db'
```

```shell
# 安装ik分词插件
bin/elasticsearch-plugin install https://chenqf-blog-image.oss-cn-beijing.aliyuncs.com/sortware/elasticsearch-analysis-ik-7.17.3.zip
# 安装icu分词插件
bin/elasticsearch-plugin install analysis-icu
```

## Kibana

```shell
docker stop kibana &> /dev/null
docker rm kibana &> /dev/null
docker run -d --name kibana --net search-net --link elasticsearch:elasticsearch \
-p 5601:5601 -v ${DATA_DIR}:/data \
-e "ELASTICSEARCH_URL=http://elasticsearch:9200" \
-e "I18N_LOCALE=zh-CN" kibana:7.17.3

# 访问: http://127.0.0.1:5601/app/dev_tools#/console
```

## IK中文分词插件

