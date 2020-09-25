### 线上经常会遇见的几个问题：
   内存泄露；
   某个进程突然 CPU 飙升；
   线程死锁；
   响应变慢。
   
   如果遇到了以上这种问题，在 线下环境 可以有各种 可视化的本地工具 支持查看。但是一旦到 线上环境，就没有这么多的 本地调试工具 支持，
   我们该如何基于 监控工具 来进行定位问题？
   我们一般会基于 数据收集 来定位问题，而数据的收集离不开 监控工具 的处理，比如：运行日志、异常堆栈、GC 日志、线程快照、堆内存快照 等。
   为了解决以上问题，我们常用的 JVM 性能调优监控工具 大致有：jps、jstat、jstack、jmap、jhat、hprof、jinfo。

   如果想要查看 Java 进程中 线程堆栈 的信息，可以选择 jstack 命令。如果要查看 堆内存，可以使用 jmap 导出并使用 jhat 来进行分析，
   包括查看 类的加载信息，GC 算法，对象 的使用情况等。可以使用 jstat 来对 JVM 进行 统计监测，包括查看各个 区内存 和 GC 的情况，
   还可以使用 hprof 查看 CPU 使用率，统计 堆内存 使用情况。下面会详细的介绍这几个工具的用法。
    
### JVM常见监控工具 & 指令
    
#### jps进程监控工具
   jps 是用于查看有权访问的 hotspot 虚拟机 的进程。当未指定 hostid 时，默认查看 本机 jvm 进程，
   否则查看指定的 hostid 机器上的 jvm 进程，此时 hostid 所指机器必须开启 jstatd 服务。
   jps 可以列出 jvm 进程 lvmid，主类类名，main 函数参数, jvm 参数，jar 名称等信息。
   
   命令格式如下：
```
usage: jps [-help]
       jps [-q] [-mlvV] [<hostid>]

Definitions:
    <hostid>:      <hostname>[:<port>]
``` 
  参数含义如下：
      
     -q: 不输出 类名称、Jar 名称 和传入 main 方法的 参数；
     -l: 输出 main 类或 Jar 的 全限定名称；
     -m: 输出传入 main 方法的 参数；
     -v: 输出传入 JVM 的参数。
     示例：jps、 jps -q、 jps -v 等
      
#### 1.jinfo配置信息查看工具
   jinfo（JVM Configuration info）这个命令作用是实时查看和调整 虚拟机运行参数。
   之前的 jps -v 命令只能查看到显示 指定的参数，如果想要查看 未显示 的参数的值就要使用 jinfo 命令。
    
```
Usage:
    jinfo [option] <pid>
        (to connect to running process)
    jinfo [option] <executable <core>
        (to connect to a core file)
    jinfo [option] [server_id@]<remote server IP or hostname>
        (to connect to remote debug server)
```
   参数含义如下：
      
    pid：本地 jvm 服务的进程 ID；
    executable core：打印 堆栈跟踪 的核心文件；
    remote server IP/hostname：远程 debug 服务的 主机名 或 IP 地址；
    server id：远程 debug 服务的 进程 ID。
    示例：jinfo pid    -- 查看正在运行的 jvm 进程的所有 参数信息。
         jinfo -flags pid -- 查看正在运行的 jvm 进程的 扩展参数
         jinfo -sysprops pid  -- 查看正在运行的 jvm 进程的 环境变量信息。
  参数选项说明如下：
    ![1571118778874](jvm.assets/jinfo.png)
  查看正在运行的 jvm 进程的 扩展参数。
```
$ jinfo -flags 9871 
Attaching to process ID 31983, please wait… 
Debugger attached successfully. 
Server compiler detected. 
JVM version is 25.91-b14 
Non-default VM flags: -XX:CICompilerCount=3 -XX:InitialHeapSize=20971520 -XX:MaxHeapFreeRatio=90 -XX:MaxHeapSize=20971520 -XX:MaxNewSize=2097152 -XX:MinHeapDeltaBytes=524288 -XX:NewSize=2097152 -XX:OldSize=18874368 -XX:+PrintGC -XX:+PrintGCDetails -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:+UseParallelGC 
Command line: -Xmx20m -Xms20m -Xmn2m -javaagent:/opt/idea-IU-181.4668.68/lib/idea_rt.jar=34989:/opt/idea-IU-181.4668.68/bin -Dfile.encoding=UTF-8
```
   查看正在运行的 jvm 进程的所有 参数信息。
```
$ jinfo 9871
Attaching to process ID 9871, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 25.91-b14
Java System Properties:

java.runtime.name = Java(TM) SE Runtime Environment
java.vm.version = 25.91-b14
sun.boot.library.path = /opt/jdk1.8.0_91/jre/lib/amd64
java.vendor.url = http://java.oracle.com/
java.vm.vendor = Oracle Corporation
path.separator = :
file.encoding.pkg = sun.io
java.vm.name = Java HotSpot(TM) 64-Bit Server VM
sun.os.patch.level = unknown
sun.java.launcher = SUN_STANDARD
user.country = CN
user.dir = /home/linchen/projects
java.vm.specification.name = Java Virtual Machine Specification
java.runtime.version = 1.8.0_91-b14
java.awt.graphicsenv = sun.awt.X11GraphicsEnvironment
os.arch = amd64
java.endorsed.dirs = /opt/jdk1.8.0_91/jre/lib/endorsed
java.io.tmpdir = /tmp
line.separator = 

java.vm.specification.vendor = Oracle Corporation
os.name = Linux
sun.jnu.encoding = UTF-8
java.library.path = /usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
java.specification.name = Java Platform API Specification
java.class.version = 52.0
sun.management.compiler = HotSpot 64-Bit Tiered Compilers
os.version = 4.15.0-24-generic
user.home = /home/linchen
user.timezone = 
java.awt.printerjob = sun.print.PSPrinterJob
file.encoding = UTF-8
java.specification.version = 1.8
user.name = linchen
java.class.path = /opt/jdk1.8.0_91/jre/lib/charsets.jar:/opt/jdk1.8.0_91/jre/lib/deploy.jar:/opt/jdk1.8.0_91/jre/lib/ext/cldrdata.jar:/opt/jdk1.8.0_91/jre/lib/ext/dnsns.jar:/opt/jdk1.8.0_91/jre/lib/ext/jaccess.jar:/opt/jdk1.8.0_91/jre/lib/ext/jfxrt.jar:/opt/jdk1.8.0_91/jre/lib/ext/localedata.jar:/opt/jdk1.8.0_91/jre/lib/ext/nashorn.jar:/opt/jdk1.8.0_91/jre/lib/ext/sunec.jar:/opt/jdk1.8.0_91/jre/lib/ext/sunjce_provider.jar:/opt/jdk1.8.0_91/jre/lib/ext/sunpkcs11.jar:/opt/jdk1.8.0_91/jre/lib/ext/zipfs.jar:/opt/jdk1.8.0_91/jre/lib/javaws.jar:/opt/jdk1.8.0_91/jre/lib/jce.jar:/opt/jdk1.8.0_91/jre/lib/jfr.jar:/opt/jdk1.8.0_91/jre/lib/jfxswt.jar:/opt/jdk1.8.0_91/jre/lib/jsse.jar:/opt/jdk1.8.0_91/jre/lib/management-agent.jar:/opt/jdk1.8.0_91/jre/lib/plugin.jar:/opt/jdk1.8.0_91/jre/lib/resources.jar:/opt/jdk1.8.0_91/jre/lib/rt.jar:/home/linchen/IdeaProjects/core_java/target/classes:/home/linchen/.m2/repository/io/netty/netty-all/4.1.7.Final/netty-all-4.1.7.Final.jar:/home/linchen/.m2/repository/junit/junit/4.12/junit-4.12.jar:/home/linchen/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/home/linchen/.m2/repository/com/lmax/disruptor/3.3.0/disruptor-3.3.0.jar:/home/linchen/.m2/repository/com/rabbitmq/amqp-client/5.3.0/amqp-client-5.3.0.jar:/home/linchen/.m2/repository/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar:/opt/idea-IU-181.4668.68/lib/idea_rt.jar
java.vm.specification.version = 1.8
sun.arch.data.model = 64
sun.java.command = com.own.learn.jvm.JinfoTest
java.home = /opt/jdk1.8.0_91/jre
user.language = zh
java.specification.vendor = Oracle Corporation
awt.toolkit = sun.awt.X11.XToolkit
java.vm.info = mixed mode
java.version = 1.8.0_91
java.ext.dirs = /opt/jdk1.8.0_91/jre/lib/ext:/usr/java/packages/lib/ext
sun.boot.class.path = /opt/jdk1.8.0_91/jre/lib/resources.jar:/opt/jdk1.8.0_91/jre/lib/rt.jar:/opt/jdk1.8.0_91/jre/lib/sunrsasign.jar:/opt/jdk1.8.0_91/jre/lib/jsse.jar:/opt/jdk1.8.0_91/jre/lib/jce.jar:/opt/jdk1.8.0_91/jre/lib/charsets.jar:/opt/jdk1.8.0_91/jre/lib/jfr.jar:/opt/jdk1.8.0_91/jre/classes
java.vendor = Oracle Corporation
file.separator = /
java.vendor.url.bug = http://bugreport.sun.com/bugreport/
sun.io.unicode.encoding = UnicodeLittle
sun.cpu.endian = little
sun.desktop = gnome
sun.cpu.isalist = 

VM Flags:
Non-default VM flags: -XX:CICompilerCount=3 -XX:InitialHeapSize=20971520 -XX:MaxHeapFreeRatio=90 -XX:MaxHeapSize=20971520 -XX:MaxNewSize=2097152 -XX:MinHeapDeltaBytes=524288 -XX:NewSize=2097152 -XX:OldSize=18874368 -XX:+PrintGC -XX:+PrintGCDetails -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:+UseParallelGC 
Command line:  -Xmx20m -Xms20m -Xmn2m -javaagent:/opt/idea-IU-181.4668.68/lib/idea_rt.jar=34989:/opt/idea-IU-181.4668.68/bin -Dfile.encoding=UTF-8
```
  查看正在运行的 jvm 进程的 环境变量信息。
```
$ jinfo -sysprops 9871 
Attaching to process ID 9871, please wait… 
Debugger attached successfully. 
Server compiler detected. 
JVM version is 25.91-b14 
java.runtime.name = Java(TM) SE Runtime Environment 
java.vm.version = 25.91-b14 
sun.boot.library.path = /opt/jdk1.8.0_91/jre/lib/amd64 
java.vendor.url = http://java.oracle.com/ 
java.vm.vendor = Oracle Corporation 
path.separator = : 
file.encoding.pkg = sun.io 
java.vm.name = Java HotSpot(TM) 64-Bit Server VM 
sun.os.patch.level = unknown 
sun.java.launcher = SUN_STANDARD 
user.country = CN 
user.dir = /home/linchen/projects 
java.vm.specification.name = Java Virtual Machine Specification 
java.runtime.version = 1.8.0_91-b14 
java.awt.graphicsenv = sun.awt.X11GraphicsEnvironment 
os.arch = amd64 
java.endorsed.dirs = /opt/jdk1.8.0_91/jre/lib/endorsed 
java.io.tmpdir = /tmp 
line.separator =
```
#### 2.jstat信息统计监控工具
  jstat 是用于识别 虚拟机 各种 运行状态信息 的命令行工具。它可以显示 本地 或者 远程虚拟机 进程中的 类装载、内存、垃圾收集、
  jit 编译 等运行数据，它是 线上 定位 jvm 性能 的首选工具。

  jstat 工具提供如下的 jvm 监控功能：
    
    1.类的加载 及 卸载 的情况；
    2.查看 新生代、老生代 及 元空间（MetaSpace）的 容量 及使用情况；
    3.查看 新生代、老生代 及 元空间（MetaSpace）的 垃圾回收情况，包括垃圾回收的 次数，垃圾回收所占用的 时间；
    4.查看 新生代 中 Eden 区及 Survior 区中 容量 及 分配情况 等。
  命令格式如下：
```
Usage: jstat -help|-options
       jstat -<option> [-t] [-h<lines>] <vmid> [<interval> [<count>]]
```
  参数含义如下:

    option: 参数选项。
        -t: 可以在打印的列加上 timestamp 列，用于显示系统运行的时间。
        -h: 可以在 周期性数据 的时候，可以在指定输出多少行以后输出一次 表头。
    vmid: Virtual Machine ID（进程的 pid）。
    lines: 表头 与 表头 的间隔行数。
    interval: 执行每次的 间隔时间，单位为 毫秒。
    count: 用于指定输出记录的 次数，缺省则会一直打印。
    示例:jstat -gc pid 
        jstat -gc pid 250 10 -- 250(时间) 10 打印10次
  参数选项说明如下：
    
    class: 显示 类加载 ClassLoad 的相关信息；
    compiler: 显示 JIT 编译 的相关信息；
    gc: 显示和 gc 相关的 堆信息；
    gccapacity: 显示 各个代 的 容量 以及 使用情况；
    gcmetacapacity: 显示 元空间 metaspace 的大小；
    gcnew: 显示 新生代 信息；
    gcnewcapacity: 显示 新生代大小 和 使用情况；
    gcold: 显示 老年代 和 永久代 的信息；
    gcoldcapacity: 显示 老年代 的大小；
    gcutil: 显示 垃圾回收信息；
    gccause: 显示 垃圾回收 的相关信息（同 -gcutil），同时显示 最后一次 或 当前 正在发生的垃圾回收的 诱因；
    printcompilation: 输出 JIT 编译 的方法信息；

##### 2.1. class 
   显示和监视 类装载、卸载数量、总空间 以及 耗费的时间。
```
$ jstat -class 9871
Loaded  Bytes     Unloaded  Bytes      Time
  7271 13325.8        1      0.9       2.98
```
  参数列表及含义如下：
  ![1571118778874](jvm.assets/jstat-class.png)

##### 2.2. compiler 
   显示虚拟机 实时编译（JIT）的 次数 和 耗时 等信息。
```
$ jstat -compiler 9871
Compiled   Failed  Invalid  Time     FailedType   FailedMethod
  3886        0       0     1.29          0
```
  参数列表及含义如下：
  ![1571118778874](jvm.assets/jstat-compiler.png)

##### 2.3. gc 
   显示 垃圾回收（gc）相关的 堆信息，查看 gc 的 次数 及 时间。
```
$ jstat -gc 9871
 S0C      S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT   
20480.0 10752.0  0.0    0.0   262128.0 130750.7  165376.0   24093.7   35456.0 33931.0 4992.0 4582.0      5    0.056   2      0.075    0.131
```
  比如下面输出的是 GC 信息，采样 时间间隔 为 250ms，采样数为 4：
```
$ jstat -gc 9871 250 4
 S0C      S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT   
20480.0 10752.0  0.0    0.0   262144.0 130750.7  165376.0   24093.7   35456.0 33931.0 4992.0 4582.0      5    0.056   2      0.075    0.131
20480.0 10752.0  0.0    0.0   262872.0 130750.7  165376.0   24093.7   35456.0 33931.0 4992.0 4582.0      5    0.056   2      0.075    0.131
20480.0 10752.0  0.0    0.0   262720.0 130750.7  165376.0   24093.7   35456.0 33931.0 4992.0 4582.0      5    0.056   2      0.075    0.131
20480.0 10752.0  0.0    0.0   262446.0 130750.7  165376.0   24093.7   35456.0 33931.0 4992.0 4582.0      5    0.056   2      0.075    0.131
```

  参数列表及含义如下：
  ![1571118778874](jvm.assets/jstat-gc.png)

##### 2.4. gccapacity 
   显示 虚拟机内存 中三代 年轻代（young)，老年代（old），元空间（metaspace）对象的使用和占用大小。
```
$ jstat -gccapacity 9871
 NGCMN     NGCMX     NGC      S0C     S1C      EC       OGCMN      OGCMX       OGC        OC         MCMN   MCMX       MC        CCSMN  CCSMX     CCSC      YGC    FGC 
 87040.0 1397760.0 372736.0 20480.0 10752.0 262144.0   175104.0  2796544.0   165376.0   165376.0      0.0 1079296.0  35456.0      0.0 1048576.0   4992.0      5     2
```
  参数列表及含义如下：
  ![1571118778874](jvm.assets/jstat-gccapacity.png)

##### 2.5. gcmetacapacity 
   显示 元空间（metaspace）中 对象 的信息及其占用量。
```
$ jstat -gcmetacapacity 8615
MCMN       MCMX        MC       CCSMN      CCSMX       CCSC     YGC   FGC    FGCT     GCT   
0.0      1079296.0   35456.0     0.0     1048576.0    4992.0     5     2    0.075    0.131
```
  参数列表及含义如下：
  ![1571118778874](jvm.assets/jstat-gcmetacapacity.png)
 
##### 2.6. gcnew 
   显示 年轻代对象 的相关信息，包括两个 survivor 区和 一个 Eden 区。
```
$ jstat -gcnew 8615
 S0C      S1C      S0U    S1U TTv MTT  DSS      EC       EU       YGC     YGCT  
20480.0 10752.0    0.0    0.0  6  15 20480.0 262144.0 131406.0      5    0.056

```
  参数列表及含义如下：
  ![1571118778874](jvm.assets/jstat-gcnew.png)

##### 2.7. gcnewcapacity 
   显示 年轻代对象 的相关信息，包括两个 survivor 区和 一个 Eden 区。
```
$ jstat -gcnewcapacity 8615
  NGCMN      NGCMX       NGC      S0CMX     S0C     S1CMX     S1C       ECMX        EC      YGC   FGC 
 87040.0   1397760.0   372736.0  465920.0  20480.0 465920.0  10752.0  1396736.0   262144.0   5     2
```
  参数列表及含义如下：
  ![1571118778874](jvm.assets/jstat-gcnewcapacity.png)




