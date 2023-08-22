# ES

和MongoDB一样, 数据量级可达PB级别, 但本身不支持事务

基于关键字关联索引

 ```shell
keyword       id
------------------------
name          1001,1002
zhang         1001
```

## 基本概念

Index看做一个库, Types相当于表, Documents相当于表的行

7.X版本, Types已经被删除, 目前Index下只能包含一个Type

### Index 索引

+ alias 索引别名, 可能有多个
+ settings 索引设置, 分片数量, 副本数量等
+ mapping 映射, 定义索引中包含哪些字段, 以及字段类型/长度/分词器等

**命名规范:**

+ 小写字母
+ 多个单词以_连接

**索引创建后以下属性不可修改:**

+ 索引名称 (可指定别名)
+ 主分片数量 (可重建索引)
  + 关闭状态下可修改
+ 字段类型 (可重建索引)

### Document 文档

文档是ES中的最小数据单元, 结构化JSON格式的记录

文档元数据, 均以_开头, 为系统字段

+ _index：文档所属的索引名
+ _type：文档所属的类型名
+ _id：文档唯一id
+ _source: 文档的原始Json数据
+ _version: 文档的版本号，修改删除操作_version都会自增1
+ _seq_no: 和_version一样，一旦数据发生更改，数据也一直是累计的。Shard级别严格递增，保证后写入的Doc的_seq_no大于先写入的Doc的_seq_no。
+ _primary_term: _primary_term主要是用来恢复数据时处理当多个文档的_seq_no一样时的冲突，避免PrimaryShard上的写入被覆盖。每当Primary
  Shard发生重新分配时，比如重启，Primary选举等，_primary_term会递增1

## 基本命令操作

### 索引

创建索引:

```shell
# 创建索引
PUT /es_db
# 创建索引时指定setting (分片数,副本数,默认分片器)
PUT /es_db
{
  "settings":{
     "index":{
        "analysis.analyzer.default.type": "ik_max_word",
        "number_of_shards" : 3,
        "number_of_replicas" : 2
     }
  }
}
```

删除索引:

```shell
DELETE /es_db
```

查询索引:
```shell
GET /es_db
```

检索索引是否存在:

```shell
HEAD /es_db
```

修改索引setting:

+ index.number_of_replicas：每个主分片的副本数。默认为 1，允许配置为 0。
+ index.refresh_interval：执行刷新操作的频率，默认为1s. 可以设置 -1 为禁用刷新。
+ index.max_result_window：from + size 搜索此索引 的最大值，默认为 10000。

```shell
PUT /es_db/_settings
{
  "index" : {
    "number_of_replicas" : 1
  }
}
```

### 索引文档映射 mapping

mapping类似Mysql中的`表结构`

```shell
# 查看index的完整mapping
GET /es_db/_mappings
# 查看指定字段的mapping
GET /es_db/_mappings/field/<field_name>
```

+ 没有隐式类型转换
+ 不支持类型修改
+ 尽量避免使用`动态映射`

`动态映射`: 文档写入时, 自动根据字段识别类型

`静态映射`: 创建索引时, 手动指定映射

```shell
PUT /user
{
  "mappings":{
    "dynamic": "strict",
    "properties":{
      "address":{
        "type": "text"
      },
      "name":{
        "type": "keyword"
      },
      "age":{
        "type": "long",
        "index": false
      }
    }
  }
}
```

+ dynamic : 控制是否可以添加新字段
  + true : 允许(默认)
  + false : 忽略新字段, 新字段不会被索引或搜索, 但包含在_source中
  + runtime : 新字段作为运行时字段添加到索引中，这些字段没有索引
  + strict : 检测到新字段, 抛出异常并拒绝文档
+ index : 是否对当前字段创建倒排索引
  + true : 创建倒排索引(默认)
  + false : 不创建倒排索引 - 该字段不可被搜索

**修改mapping:** 对已有字段, 一旦有数据写入, 不可再修改

```shell
PUT /user/_mapping
{
  "dynamic":true
}

```

### 文档 Document

插入数据:

```shell
# 插入数据, 指定id:1
PUT /user/_doc/1
{
  "name": "fox",
  "address": "广州白云山公园",
  "age":30
}
```

```shell
# 插入数据, ES自动生成id
POST /user/_doc
{
  "name": "fox",
  "address": "广州白云山公园",
  "age":30
}
```

根据ID查看数据:
```shell
GET /user/_doc/<ID>
```

查看所有数据:
```shell
GET /user/_search
```

查看所有数据-模糊匹配:
```shell
GET /user/_search
{
  "query":{
    "match":{
      "address":"广州"
    }
  }
}
```

修改数据:
```shell
POST /user/_update/<ID>
{
  "doc":{
    "name":"chenqf"
  }
}
```

删除数据:
```shell
DELETE /user/_doc/<ID>
```

#### 更新 - 乐观锁

高并发场景下使用`乐观锁`机制修改文档, 要带上当前文档的`_seq_n`o和`_primary_term`

```shell
POST /user/_update/<ID>?if_seq_no=<SEQ_NO>&if_primary_term=<PRIMARY_TERM>
{
  "doc":{
    "name":"chenqf1"
  }
}
```

### ReIndex 重建索引

需要并更当前Index的字段映射:

1. 重新建立一个新索引
2. 将之前索引数据导入新索引
3. 删除原来索引
4. 为新索引起个别名

实现了索引的平滑过渡, 且0停机

```shell
# 把之前索引里的数据导入到新的索引里
POST _reindex
{
  "source":{
    "index": "user"
  },
  "dest":{
    "index": "user_new"
  }
}
```

```shell
# 删除后添加别名
DELETE /user
PUT /user_new/_alias/user
```

### 批量操作

+ 可对不同Index进行操作
+ 单挑失败, 不影响其他操作(无事务)
+ 返回结构包括每一条的执行结果

+ 请求方式: POST
+ 请求地址: _bulk
+ 请求参数: 偶数行参数
  + 奇数行: 指定操作类型及操作的对象(index,type,id)
  + 偶数行: 操作的数据

```shell
# 批量创建
POST /_bulk
{"create":{"_index":"user"}}
{"name": "fox100","address": "广州白云山公园","age":30}
{"create":{"_index":"user"}}
{"name": "fox100","address": "广州白云山公园","age":30}
```

```shell
# 批量删除
POST /_bulk
{"delete":{"_index":"user","_id":"1001"}}
{"delete":{"_index":"user","_id":"1002"}}
```

```shell
# 批量修改
POST /_bulk
{"update":{"_index":"user","_id":"1001"}}
{"name": "fox100","address": "广州白云山公园","age":30}
{"update":{"_index":"user","_id":"1002"}}
{"name": "fox100","address": "广州白云山公园","age":30}
```

以上操作, 还可组合应用

#### 批量查询

+ _mget : 通过id查询, 可指定不同Index
+ _msearch : 通过字段查询, 可指定不同Index

```shell
# 获取不同Index的数据
GET /_mget
{"docs":[{"_index":"user","_id":"1001"},{"_index":"user","_id":"1002"}]}

# 批量获取同一Index的数据
GET /user/_mget
{"ids":["1","2"]}
```

```shell
# 根据条件查询多个Index的数据 从第0条开始, 查2条
GET /_msearch
{"index":"user"}
{"query" : {"match" : {"address":"广州"}}, "from" : 0, "size" : 2}
{"index":"user"}
{"query" : {"term" : {"name":"fox100"}}, "from" : 0, "size" : 2}
```

### 复杂查询

默认查询10条记录
```shell
GET /user/_doc/_search
```

DSL Query:

```shell
# match 匹配查询, 会对文本分词后匹配
GET /user/_search
{
  "query":{
    "match":{
      "address":"广州"
    }
  }
}
```

```shell
# term 精确匹配, 不会进行分词, 针对keyword类型进行查询
GET /user/_search
{
  "query":{
    "term":{
      "name":"fox"
    }
  }
}
```

```shell
# 根据查询项更新
POST /user/_update_by_query
{
  "query":{
    "term":{
      "name":"fox"
    }
  },
  "script":{
    "source": "ctx._source.age = 20"
  }
}
```

基于某个字段进行查询:

```shell
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "query":{
        "match":{
            "category":"小米"
        }
    }
}'
```

分页查询:

```shell
# 查第1页, 每页10条
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "query":{
        "match":{
            "category":"小米"
        }
    },
    "from": 0,
    "size": 10
}'
```



排序:

```shell
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "query":{
        "match":{
            "category":"小米"
        }
    },
    "from": 0,
    "size": 10,
    "sort": {
        "price": {
            "order": "desc"
        }
    }
}'
```




范围查询:

```shell
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "query":{
        "bool":{
            "should":[
                {
                    "match":{
                        "category": "小米"
                    }
                },
                {
                    "match":{
                        "category": "华为"
                    }
                }
            ],
            "filter" : {
                "range" : {
                    "price" : {
                        "gt" : 5000
                    }
                }
            }
        }
    }
}'
```

全文检索, 进行分词倒排索引检索:

```shell
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "query":{
        "match":{
            "category":"小华"
        }
    }
}'
```

完全匹配:

```shell
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "query":{
        "match_phrase":{
            "category":"小米"
        }
    }
}'
```

高亮匹配字段:

```shell
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "query":{
        "match_phrase":{
            "category":"小米"
        }
    },
    "highlight": {
        "fields" : {
            "category" : {}
        }
    }
}'
```

聚合查询 - 分组统计:

```shell
# 统计价格相等的有几个, 分组名随意起名, size:0 代表不查询原始数据
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "aggs":{
        "price_group":{
            "terms":{
                "field" : "price"
            }
        }
    },
    "size" : 0
}'
```

聚合查询 - 查询平均值:

```shell
# 查询数据price字段的平均值, 分组名随意起名, size:0 代表不查询原始数据
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "aggs":{
        "price_avg":{
            "avg":{
                "field" : "price"
            }
        }
    },
    "size" : 0
}'
```

## 分词插件

建议使用IK分词器, [GITHUB:elasticsearch-analysis-ik](https://github.com/medcl/elasticsearch-analysis-ik)

```shell
#查看已安装插件
bin/elasticsearch-plugin list
#安装插件
bin/elasticsearch-plugin install analysis-icu
#删除插件
bin/elasticsearch-plugin remove analysis-icu
```

**查看分词:**

```shell
POST _analyze
{
  "analyzer":"ik_max_word",
  "text":"中华人民共和国"
}
```

## 整合 Spring-Boot

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

```yaml
spring:
  application:
    name: ES-demo
  elasticsearch:
    uris: http://${ENV_CLOUD_ID}:9200
    connection-timeout: 3s
```

## 企业级应用场景

+ 搜索
+ 日志实时分析
  + 采集: Logstash / Beats
  + 存储/查询 : Elasticsearch
  + 可视化: Kibana
+ 智能BI

## Kibana可视化

## 聚合的理解

## 日志分析平台

## 监控数据汇总分析

## Mysql 导入 ES

1. 同步双写
2. 异步双写
3. 基于SQL抽取
4. 基于Binlog同步
    1. canal

## Other

keyword : 部分词, 全匹配
match 和 term 的区别
index:false 字段不可被搜索