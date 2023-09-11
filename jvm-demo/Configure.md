

-XX:+PrintFlagsInitial : 查看所有参数的默认初始值
-XX:+PrintFlagsFinal : 查看所有参数的最终值
-XX:+PrintGCDetails : 查看详细的GC情况
-Xms : 初始堆空间大小 - 默认物理内存的 1/64
-Xmx : 最大堆空间大小 - 默认物理内存的 1/4
-Xmn : 设置新生代的大小
-XX:NewRatio : 配置老年代与新生代在堆结构的比例
-XX:SurvivorRatio : 设置新生代中Eden和S0/S1空间的比例
-XX:MaxTenuringThreshold: 设置新生代中Survivor区中对象的最大年两
-XX:UseTLAB : 开启TLAB
-XX:TLABWasteTargetPercent : TLAB空间占整个Eden空间的百分比
-XX:+DoEscapeAnalysis : 开启逃逸分析实现栈上对象分配, 默认开启

```shell
# 查看某个参数的值是多少
jinfo -flag NewRatio <pid>
```


Jconsole
VisualVM
Jprofiler
Java Flight Recorder
GCViewer
GC Easy


