# 分布式日志处理


logback + elk

filebean + elk

filebean + kafka + elk



听对话这位老哥对链路跟踪这块没什么概念，应该听说过Elastic APM。国内Java生态圈Skywalking应该占了半壁江山吧。微服务架构日志的记录应该是直接输出日志到控制台，通过filebeat或者promtail去收集，日志和链路这块最好能统一trace_id,方便定位与分析问题。日志这块除了ELK,EFK系列，Loki也挺好用的。再请教个问题，链路采样率，不知如何设置更合理。