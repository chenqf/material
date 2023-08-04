# MongoDB

## 单机安装

```shell
export DATA_DIR=/docker/mongo/standalone/data
export LOG_DIR=/docker/mongo/standalone/logs
export USERNAME=fox
export PASSWORD=fox 
export PORT=27017
export CONTAINER_NAME=mongodb
mkdir -p ${DATA_DIR}
mkdir -p ${LOG_DIR}
docker stop ${CONTAINER_NAME} &> /dev/null
docker rm ${CONTAINER_NAME} &> /dev/null

docker run --name ${CONTAINER_NAME} -p ${PORT}:27017 \
-e MONGO_INITDB_ROOT_USERNAME=${USERNAME} \
-e MONGO_INITDB_ROOT_PASSWORD=${PASSWORD} \
-d mongo:6.0.5 --wiredTigerCacheSizeGB 1

docker exec -it ${CONTAINER_NAME} mongosh -u ${USERNAME} -p ${PASSWORD}

#docker run --name ${CONTAINER_NAME} -v ${DATA_DIR}:/data/db -v ${LOG_DIR}:/var/log/mongodb \
#-e MONGO_INITDB_ROOT_PASSWORD=${PASSWORD} -e MONGO_INITDB_ROOT_USERNAME=${USERNAME} \
#-p ${PORT}:27017 -d mongo:6.0.5 --wiredTigerCacheSizeGB 1
```

工具: COMPOSE