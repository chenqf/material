# ES

查询: 有边界, 具体的查询
搜索: 无边界, 基于分词器的倒排索引表

## 数据格式

Index看做一个库, Types相当于表, Documents相当于表的行

7.X版本, Types已经被删除, 目前Index下只能包含一个Type

## 倒排索引

基于关键字关联索引

```shell
keyword       id
------------------------
name          1001,1002
zhang         1001
```

### 基本操作

创建索引: 等同于创建mysql中的数据库

```shell
curl --location --request PUT 'http://127.0.0.1:9200/<index-name>'
```

查看索引: 

```shell
curl --location --request GET 'http://127.0.0.1:9200/<index-name>'
```

查看所有索引:

```shell
curl --location --request GET 'http://127.0.0.1:9200/_cat/indices?v'
```

删除索引:

```shell
curl --location --request DELETE 'http://127.0.0.1:9200/<index-name>'
```

添加数据: 自动生成ID

```shell
curl --location --request POST 'http://127.0.0.1:9200/<Index-name>/_doc' \
--header 'Content-Type: application/json' \
--data-raw '{
    "title":"小米手机",
    "category":"小米",
    "images": "http://dddd.jpg",
    "price":3999.00
}'
```

添加数据: 手动指定ID

```shell
curl --location --request POST 'http://127.0.0.1:9200/<Index-name>/_doc/<ID>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "title":"小米手机",
    "category":"小米",
    "images": "http://dddd.jpg",
    "price":3999.00
}'
```

基于ID查询数据:

```shell
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_doc/<ID>'
```

查询Index下所有数据:

```shell
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search'
```

修改数据: 全覆盖修改, _version会+1 (用的不多)

```shell
curl --location --request PUT 'http://127.0.0.1:9200/<Index-name>/_doc/<ID>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "title":"小米手机2",
    "category":"小米2",
    "images": "http://dddd.jpg",
    "price":4999.00
}'
```

修改数据: 更新局部数据, _version会+1

```shell
curl --location --request POST 'http://127.0.0.1:9200/<Index-name>/_update/<ID>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "doc":{
        "title":"华为手机"
    }
}'
```

删除数据: 

```shell
curl --location --request DELETE 'http://127.0.0.1:9200/<Index-name>/_doc/<ID>' 
```

### 复杂查询

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

指定像查询的字段:
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
    "_source": ["title"]
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

多条件同时成立:

```shell
curl --location --request GET 'http://127.0.0.1:9200/<Index-name>/_search' \
--header 'Content-Type: application/json' \
--data-raw '{
    "query":{
        "bool":{
            "must":[
                {
                    "match":{
                        "category": "小米"
                    }
                },
                {
                    "match":{
                        "price": 3999.0
                    }
                }
            ]
        }
    }
}'
```

多条件某个成立:

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
            ]
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

增加映射: TODO

text : 分词
index : 可索引查询
keyword : 不可分词, 必须完整匹配



## Mysql 导入 ES
