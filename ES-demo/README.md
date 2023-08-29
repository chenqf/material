# ES

和MongoDB一样, 数据量级可达PB级别, 但本身不支持事务

基于关键字关联索引

 ```shell
keyword       id
------------------------
name          1001,1002
zhang         1001
```

## 集群架构

+ 高可用性
  + 服务可用性: 允许有服务节点停止服务
  + 数据可用性: 部分节点丢失, 不会丢失数据
+ 可扩展性
  + 请求量提升/数据量提升(将数据分布到所有节点上)

ES集群架构的优势:

+ 提高系统的可用性, 部分节点停止服务, 整个集群的服务不受影响
+ 存储的水平扩容


主节点和副本节点都写入后, 才返回, 保证数据一致性
日志类场景, 没必要分配副本节点(允许数据丢失)

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

`静态映射`: 创建索引时, 手动指定映射, 建议text类型指定一个keyword子类型

```shell
PUT /user
{
  "mappings":{
    "dynamic": "strict",
    "properties":{
      "address":{
        "type": "text",
        "fields":{
          "keyword":{
            "type": "keyword"
          }
        }
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

### DSL Query:

+ _source : 是一个数组, 用来指定展示的字段
+ size : 指定查询结果中指定的条数, 默认10条
+ from : 显示跳过的初始结构数量, 默认为0
+ sort : 指定字段排序, 会让评分失效

#### query.match_all

匹配所有文档, `默认查询10条记录`

```shell
GET /user/_doc/_search
{
  "query":{
    "match_all":{}
  },
  "_source": ["name","address"],
  "sort":[
    {
      "age":"desc"
    }
  ],
  "form":2,
  "size":5
}
```

#### 术语级别查询

搜索内容不经分词直接用于文本匹配, 搜索对象多为`非text类型`字段

+ query.term 类似于SQL中的`=`查询
+ terms
+ range

```shell
GET /es_db/_search
{
  "query": {
    "term": {
      "age": {
        "value": 28
      }
    }
  }
}
```

对于text类型的字段, 在定义mapping结构时, 建议定义子类型为keyword, 使用子类型进行精确查询

```shell
GET /es_db/_search
{
  "query":{
    "term": {
      "address.keyword": {
        "value": "广州白云山公园"
      }
    }
  }
}
```

默认户进行相关度算分, 可通过Constant Score将查询转换为Filtering, 避免算分, 提高性能

```shell
GET /es_db/_search
{
  "query": {
    "constant_score": {
      "filter": {
        "term": {
          "address.keyword": "广州白云山公园"
        }
      }
    }
  }
}
```

term处理多值字段时, 查询时包含, 不是等于

```shell
# {"name":"小明","interest":["跑步","篮球"]}

POST /employee/_search
{
  "query": {
    "term": {
      "interest.keyword": {
        "value": "跑步"
      }
    }
  }
}
```

`terms`: 精确匹配指定字段中包含的任何一个词项

```shell
POST /es_db/_search
{
  "query": {
    "terms": {
      "remark.keyword": ["java assistant", "java architect"]
    }
  }
}
```

`exists`: 查询文档中是否存在对应的字段

```shell
GET /es_db/_search
{
  "query": {
    "exists": {
      "field": "remark"
    }
  }
}
```

`ids`: 值为数组类型, 用在根据一组Id获取多个对应文档

```shell
GET /es_db/_search
{
  "query": {
    "ids": {
      "values": [1,2]
    }
  }
}
```

**范围查询**

+ range: 范围关键字
+ gte: 大于等于
+ lte: 小于等于
+ gt: 大于
+ lt: 小于
+ now: 当前时间

```shell
POST /es_db/_search
{
  "query": {
    "range": {
      "age": {
        "gte": 25,
        "lte": 28
      }
    }
  }
}

GET /product/_search
{
  "query": {
    "range": {
        "date": {
        "gte": "2018-01-01"
        }
    }
  }
}
```

`prefix`: 前缀查询, 遍历所有倒排索引, 并找到所有前缀匹配的数据

```shell
GET /es_db/_search
{
  "query": {
    "prefix": {
      "address": {
        "value": "广州"
      }
    }
  }
}
```

`wildcard`: 通配符查询, 不只比较开头, 支持更复杂的匹配模式

```shell
GET /es_db/_search
{
  "query": {
    "wildcard": {
      "address": {
        "value": "*白*"
      }
    }
  }
}
```

`fuzzy`: 模糊匹配, 搜索时, 有时会打错字, fuzzy支持错别字的情形

+ fuzziness: 表示输入的错别字可以为几个 0-2 之间, 默认为0
+ prefix_length: 表示输入的关键字和ES查询的内容开头的第n个字符必须完全匹配
  + 如果是1, 表示开头的字必须匹配
  + 默认为0
  + 加大该值, 可提高效率和准确率

```shell
GET /es_db/_search
{
  "query": {
    "fuzzy": {
      "address": {
        "value": "白运山",
        "fuzziness": 1 
      }
    }
  }
}
```

#### 全文检索

会对输入的文本进行分词, 对于分词结果进行查询

`match`: 会对查找的内容进行分词, 然后按分词匹配查找

+ query: 支持匹配的值
+ operator: 匹配的条件类型
  + and: 查询项分词后, 每个词项都匹配
  + or: 查询项分词后, 有一个词项匹配即可(默认)
+ minmum_should_match
  + 当operator参数设置为or时,minnum_should_match参数用来控制匹配的分词的最少数量。



```shell
#match 分词后or的效果
GET /es_db/_search
{
  "query": {
    "match": {
      "address": "广州白云山公园"
    }
  }
}

# 分词后 and的效果
GET /es_db/_search
{
  "query": {
    "match": {
      "address": {
        "query": "广州白云山公园",
        "operator": "and"
      }
    }
  }
}

# 最少匹配广州，公园两个词
GET /es_db/_search
{
  "query": {
    "match": {
      "address": {
        "query": "广州公园",
        "minimum_should_match": 2
      }
    }
  }
}
```

`multi_match`: 多字段查询, 一个查询值, 在多个字段进行分词查询

```shell
GET /es_db/_search
{
  "query": {
    "multi_match": {
      "query": "长沙张龙",
      "fields": [
        "address",
        "name"
      ]
    }
  }
}
```

`match_phrase`: 短语搜索, 对搜索文本进行分词, 到倒排索引中进行查找, 并要求`分词相邻` , 通过`slop`设置分词出现的最大间隔

```shell
GET /es_db/_search
{
  "query": {
    "match_phrase": {
      "address": "广州白云山",
      "slop": 2
    }
  }
}
```

`query_string`: 允许我们在单个查询字符串中指定AND | OR | NOT条件，同时也和 multi_match query 一样，支持多字段搜索

query_string是在所有字段中搜索，范围更广泛。

AND | OR | NOT 要求大写

```shell
# 未指定字段查询
GET /es_db/_search
{
  "query": {
    "query_string": {
      "query": "赵六 AND 橘子洲"
    }
  }
}

# 指定单个字段查询
GET /es_db/_search
{
  "query": {
    "query_string": {
      "default_field": "address",
      "query": "白云山 OR 橘子洲"
    }
  }
}

# 指定多个字段查询
GET /es_db/_search
{
  "query": {
    "query_string": {
      "fields": ["name","address"],
      "query": "张三 OR (广州 AND 王五)"
    }
  }
}
```

#### bool - 布尔查询

按照布尔逻辑条件组织多条查询语句, 只有符合整个布尔条件的文档才会被搜索出来

+ must : 可包含多个查询语句, 每个条件均满足才能被搜索到
+ should: 可包含多个查询语句, 不存在must和filter时, 至少满足一个条件
+ filter: 可包含多个过滤条件, 每个条件均满足才能被搜索到
+ must_not: 可包含多个过滤条件, 每个条件均不满足才能被搜索到


```shell
GET /books/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "title": "java编程"
          }
        },
        {
          "match": {
            "description": "性能优化"
          }
        }
      ]
    }
  }
}

GET /books/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "match": {
            "title": "java编程"
          }
        },
        {
          "match": {
            "description": "性能优化"
          }
        }
      ],
      "minimum_should_match": 1
    }
  }
}

GET /books/_search
{
  "query": {
    "bool": {
      "filter": [
        {
          "term": {
            "language": "java"
          }
        },
        {
          "range": {
            "publish_time": {
              "gte": "2010-08-01"
            }
          }
        }
      ]
    }
  }
}
```

#### 高亮

`highlight`: 可以让符合条件的文档中的关键词高亮

+ pre_tags: 前缀标签
+ post_tags: 后缀标签
+ tags_schema: 设置为styled可以使用内置高亮样式
+ require_field_match: 多字段高亮需要设置为false

```shell
GET /products/_search
{
  "query": {
    "term": {
      "name": {
        "value": "牛仔"
      }
    }
  },
  "highlight": {
    "post_tags": ["</span>"],
    "pre_tags": ["<span style='color:red'>"],
    "fields": {
      "*":{}
    }
  }
}

```

## 深度分页

在查询页数特别大, 当from + size大于10000的时候, 就会出现问题

ES通过参数index.max_result_window用来限制单次查询满足查询条件的结果窗口的大小，其默认值为10000。

### 问题

ES分页流程:

1. 数据存储在各个分片中，协调节点将查询请求转发给各个节点，当各个节点执行搜索后，将排序后的前N条数据返回给协调节点。
2. 协调节点汇总各个分片返回的数据，再次排序，最终返回前N条数据给客户端
3. 这个流程会导致一个深度分页的问题，也就是翻页越多，性能越差，甚至导致ES出现OOM。

每次有序的查询都会在每个分片中执行单独的查询，然后进行数据的二次排序，而这个二次排序的过程是发生在内存中的

### 解决方案

#### 避免使用深度分页

谷歌/百度, 都没有跳页功能, 限制用户的查询数量

淘宝/京东, 仅提供前100页的商品数据, 限制用户查询的数量

#### 滚动查询 - Scroll Search

在第一次搜索的时候, 保存一个当时的快照, 之后仅基于该快照搜索数据, 用户看不到期间变更的数据

不适合实时性要求高的搜索场景, 官方已经不推荐使用, `常用于数据迁移`

```shell
# scroll=1m,说明采用游标查询，保持游标查询窗口1分钟
GET /es_db/_search?scroll=1m
{
  "query": { "match_all": {}},
  "size": 2
}
```

查询结构除返回前2条记录, 还返回了一个游标ID值_scroll_id

从第二次查询开始，每次查询都要指定_scroll_id参数(scroll_id不变):

```shell
GET /_search/scroll
{
  "scroll": "1m",
  "scroll_id" :"scroll_id"
}
```

使用后删除:

```shell
DELETE /_search/scroll
{
  "scroll_id" : "scroll_id"
}
```

#### search_after

使用移动贯标, 作为上一页的结果来帮助检索下一页, `不适合大幅度跳页查询`

搜索的查询和排序参数须保持不变

1. 获取索引PIT: 创建一个时间点, 保留搜索中的当前索引状态

```shell
POST /es_db/_pit?keep_alive=1m
#返回结果，会返回一个PID的值
{
 "id" :"pid"
}
```

2. 根据PIT首次查询

```shell
GET /_search
{
  "query": {
    "match_all": {}
  },
  "pit": {
    "id": "pid",
    "keep_alive": "1m"
  },
  "size": 2,
  "sort": [
    {"_id": "asc"} 
  ]
}
```

3. 根据search_after和pit进行翻页查询

search_after的值为上一次查询返回的sort值

```shell
GET /_search
{
  "query": {
    "match_all": {}
  },
  "pit": {
    "id": "pit",
    "keep_alive": "1m"
  },
  "size": 2,
  "sort": [
    {"_id": "asc"} 
  ],
  "search_after": [
          "8",
          7
        ]
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