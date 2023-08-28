PUT /es_db
{
"settings" : {
"index" : {
"analysis.analyzer.default.type": "ik_max_word"
}
}
}

# 创建文档,指定id
11
PUT /es_db/_doc/1
12
{
13
"name": "张三",
14
"sex": 1,
15
"age": 25,
16
"address": "广州天河公园",
17
"remark": "java developer"
18
}
19
PUT /es_db/_doc/2
20
{
21
"name": "李四",
22
"sex": 1,
23
"age": 28,
24
"address": "广州荔湾大厦",
25
"remark": "java assistant"
26
}
27
28
PUT /es_db/_doc/3
29
{
30
"name": "王五",
31
"sex": 0,
32
"age": 26,
33
"address": "广州白云山公园",
34
"remark": "php developer"
35
}
36
37
PUT /es_db/_doc/4
38
{
39
"name": "赵六",
40
"sex": 0,
41
"age": 22,
42
"address": "长沙橘子洲",
43
"remark": "python assistant"
44
}
45
46
PUT /es_db/_doc/5
47
{
48
"name": "张龙",
49
"sex": 0,
50
"age": 19,
51
"address": "长沙麓谷企业广场",
52
"remark": "java architect assistant"
53
}
54
使用match_all，匹配所有文档，默认只会返回10条数据。
原因：_search查询默认采用的是分页查询，每页记录数size的默认值为10。如果想显示更多数据，指
定size
55
PUT /es_db/_doc/6
56
{
57
"name": "赵虎",
58
"sex": 1,
59
"age": 32,
60
"address": "长沙麓谷兴工国际产业园",
61
"remark": "java architect"
62
}
63
64
PUT /es_db/_doc/7
65
{
66
"name": "李虎",
67
"sex": 1,
68
"age": 32,
69
"address": "广州番禺节能科技园",
70
"remark": "java architect"
71
}
72
73
PUT /es_db/_doc/8
74
{
75
"name": "张星",
76
"sex": 1,
77
"age": 32,
78
"address": "武汉东湖高新区未来智汇城",
79
"remark": "golang developer"
80
}