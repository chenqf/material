# MYSQL

## 事务























每写一个sql都要考虑sql是怎么执行的

避免虚拟列查询, 会导致全分片查询

第一要务, 表数据分配均匀

时间分片,数据峰值不固定

## 分库分表策略


### inline策略 - 单一键分片
尽量不要用in (id1,id2,id3) , 确保id都在一个分片中, 否则会全分片表扫描
无法使用between查询, 需要单独配置允许范围查询 - 全分片表扫描


### complex策略 按多个分片键分片


### class_base策略 定制一个复杂的类实现分片策略

海量数据场景下, 不要使用存储过程


可分多个策略, 一个策略用于插入, 其他策略用于查询


mp 字段名尽量不要用id, 用了id, 默认吧id作为主键, 默认生成雪花算法, 不会将id交由shardingJDBC处理

mysql中的查询区分大小写么
