### 1. Java服务，CPU100%问题如何快速定位？
  
  简要步骤如下：
  
  （1）找到最耗 CPU 的进程；
  
  （2）找到最耗 CPU 的线程；
  
  （3）查看堆栈，定位线程在干嘛，定位对应代码；
  
#### 1.1 ``步骤一、找到最耗 CPU 的进程

    工具：top
    
    方法：
    
    执行 top -c ，显示进程运行信息列表
    
    键入 P (大写 p)，进程按照 CPU 使用率排序
    


   
   如上图，最耗 CPU 的进程 PID 为 10765。
   
#### 1.2 步骤二：找到最耗 CPU 的线程
   
   工具：top
   
   方法：
   
   top -Hp 10765 ，显示一个进程的线程运行信息列表
   
   键入 P (大写 p)，线程按照 CPU 使用率排序
   
   ![](jvm.assets/cpu100-2.jpg)
   
   如上图，进程 10765 内，最耗 CPU 的线程 PID 为 10804。
   
   
#### 1.3 步骤三：查看堆栈，定位线程在干嘛，定位对应代码

   首先，将线程 PID 转化为 16 进制。
   
   工具：printf
   
   方法：printf "%x\n" 10804
   
   ![](jvm.assets/cpu100-3.png)
   
    如上图，10804 对应的 16 进制是 0x2a34，当然，这一步可以用计算器。
   
    之所以要转化为 16 进制，是因为堆栈里，线程 id 是用 16 进制表示的。
   
   
   接着，查看堆栈，找到线程在干嘛。
   
   工具：jstack
   
   方法：jstack 10765 | grep '0x2a34' -C5 --color
   
    打印进程堆栈
   
    通过线程 id，过滤得到线程堆栈
    
   ![](jvm.assets/cpu100-4.jpg)
   
   如上图，找到了耗 CPU 高的线程对应的线程名称 “AsyncLogger-1”，以及看到了该线程正在执行代码的堆栈。
   最后，根据堆栈里的信息，找到对应的代码.
   

### 2. 用jstack加进程id查找死锁

```java
package com.wlz.jvm;

/**
 *  死锁
 * @author wlz
 * @date 2022-03-23  11:52 下午
 */
public class DeadLockTest {

    private static Object lock1 = new Object();
    private static Object lock2 = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (lock1) {
                try {
                    System.out.println("thread1 begin");
                    Thread.sleep(5000);
                }catch (InterruptedException e) {

                }
                synchronized (lock2) {
                    System.out.println("thread1 end");
                }
            }
        }).start();

        new Thread(() -> {
            synchronized (lock2) {
                try {
                    System.out.println("thread2 begin");
                    Thread.sleep(5000);
                }catch (InterruptedException e) {

                }
                synchronized (lock1) {
                    System.out.println("thread2 end");
                }
            }
        }).start();

        System.out.println("main thread end");

    }
}

```

```shell
jps -l 
jstack 50835
```

```shell
wlz@wlzdeMacBook-Pro Java_knowledge_system % jps
50834 Launcher
707 
50835 DeadLockTest
50854 Jps
31706 
wlz@wlzdeMacBook-Pro Java_knowledge_system % jstack 50835
```

  结果:

```
2022-03-23 23:55:33
Full thread dump OpenJDK 64-Bit Server VM (25.312-b07 mixed mode):

"Attach Listener" #14 daemon prio=9 os_prio=31 tid=0x0000000136130800 nid=0x5903 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"DestroyJavaVM" #13 prio=5 os_prio=31 tid=0x000000015600e800 nid=0x1603 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Thread-1" #12 prio=5 os_prio=31 tid=0x0000000156086800 nid=0x5703 waiting for monitor entry [0x0000000172a3e000]
   java.lang.Thread.State: BLOCKED (on object monitor)
        at com.wlz.jvm.DeadLockTest.lambda$main$1(DeadLockTest.java:37)
        - waiting to lock <0x000000076ac264f0> (a java.lang.Object)
        - locked <0x000000076ac26500> (a java.lang.Object)
        at com.wlz.jvm.DeadLockTest$$Lambda$2/1096979270.run(Unknown Source)
        at java.lang.Thread.run(Thread.java:748)

"Thread-0" #11 prio=5 os_prio=31 tid=0x00000001608e0800 nid=0x5603 waiting for monitor entry [0x0000000172832000]
   java.lang.Thread.State: BLOCKED (on object monitor)
        at com.wlz.jvm.DeadLockTest.lambda$main$0(DeadLockTest.java:23)
        - waiting to lock <0x000000076ac26500> (a java.lang.Object)
        - locked <0x000000076ac264f0> (a java.lang.Object)
        at com.wlz.jvm.DeadLockTest$$Lambda$1/2003749087.run(Unknown Source)
        at java.lang.Thread.run(Thread.java:748)

"Service Thread" #10 daemon prio=9 os_prio=31 tid=0x0000000156061800 nid=0x3f03 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C1 CompilerThread3" #9 daemon prio=9 os_prio=31 tid=0x000000015701f800 nid=0x4103 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread2" #8 daemon prio=9 os_prio=31 tid=0x000000016003b000 nid=0x3d03 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread1" #7 daemon prio=9 os_prio=31 tid=0x000000016003a000 nid=0x4303 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread0" #6 daemon prio=9 os_prio=31 tid=0x0000000160039800 nid=0x3b03 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Monitor Ctrl-Break" #5 daemon prio=5 os_prio=31 tid=0x000000016081b000 nid=0x3903 runnable [0x00000001719de000]
   java.lang.Thread.State: RUNNABLE
        at java.net.SocketInputStream.socketRead0(Native Method)
        at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
        at java.net.SocketInputStream.read(SocketInputStream.java:171)
        at java.net.SocketInputStream.read(SocketInputStream.java:141)
        at sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:284)
        at sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:326)
        at sun.nio.cs.StreamDecoder.read(StreamDecoder.java:178)
        - locked <0x000000076ac94678> (a java.io.InputStreamReader)
        at java.io.InputStreamReader.read(InputStreamReader.java:184)
        at java.io.BufferedReader.fill(BufferedReader.java:161)
        at java.io.BufferedReader.readLine(BufferedReader.java:324)
        - locked <0x000000076ac94678> (a java.io.InputStreamReader)
        at java.io.BufferedReader.readLine(BufferedReader.java:389)
        at com.intellij.rt.execution.application.AppMainV2$1.run(AppMainV2.java:47)

"Signal Dispatcher" #4 daemon prio=9 os_prio=31 tid=0x0000000160814000 nid=0x4403 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Finalizer" #3 daemon prio=8 os_prio=31 tid=0x0000000156813800 nid=0x4b03 in Object.wait() [0x0000000171422000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x000000076ab08ef0> (a java.lang.ref.ReferenceQueue$Lock)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)
        - locked <0x000000076ab08ef0> (a java.lang.ref.ReferenceQueue$Lock)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:165)
        at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:216)

"Reference Handler" #2 daemon prio=10 os_prio=31 tid=0x0000000156810800 nid=0x3003 in Object.wait() [0x0000000171216000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x000000076ab06c08> (a java.lang.ref.Reference$Lock)
        at java.lang.Object.wait(Object.java:502)
        at java.lang.ref.Reference.tryHandlePending(Reference.java:191)
        - locked <0x000000076ab06c08> (a java.lang.ref.Reference$Lock)
        at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)

"VM Thread" os_prio=31 tid=0x000000015605b800 nid=0x2f03 runnable 

"ParGC Thread#0" os_prio=31 tid=0x0000000156010800 nid=0x2007 runnable 

"ParGC Thread#1" os_prio=31 tid=0x0000000156011000 nid=0x1e03 runnable 

"ParGC Thread#2" os_prio=31 tid=0x0000000160008800 nid=0x5403 runnable 

"ParGC Thread#3" os_prio=31 tid=0x000000015601c800 nid=0x5303 runnable 

"ParGC Thread#4" os_prio=31 tid=0x0000000160009000 nid=0x2c03 runnable 

"ParGC Thread#5" os_prio=31 tid=0x0000000160808800 nid=0x5003 runnable 

"ParGC Thread#6" os_prio=31 tid=0x0000000160809000 nid=0x2e03 runnable 

"ParGC Thread#7" os_prio=31 tid=0x000000015601d800 nid=0x4e03 runnable 

"VM Periodic Task Thread" os_prio=31 tid=0x0000000156062800 nid=0xa903 waiting on condition 

JNI global references: 320


Found one Java-level deadlock:
=============================
"Thread-1":
  waiting to lock monitor 0x00000001560614b0 (object 0x000000076ac264f0, a java.lang.Object),
  which is held by "Thread-0"
"Thread-0":
  waiting to lock monitor 0x000000015605ec20 (object 0x000000076ac26500, a java.lang.Object),
  which is held by "Thread-1"

Java stack information for the threads listed above:
===================================================
"Thread-1":
        at com.wlz.jvm.DeadLockTest.lambda$main$1(DeadLockTest.java:37)
        - waiting to lock <0x000000076ac264f0> (a java.lang.Object)
        - locked <0x000000076ac26500> (a java.lang.Object)
        at com.wlz.jvm.DeadLockTest$$Lambda$2/1096979270.run(Unknown Source)
        at java.lang.Thread.run(Thread.java:748)
"Thread-0":
        at com.wlz.jvm.DeadLockTest.lambda$main$0(DeadLockTest.java:23)
        - waiting to lock <0x000000076ac26500> (a java.lang.Object)
        - locked <0x000000076ac264f0> (a java.lang.Object)
        at com.wlz.jvm.DeadLockTest$$Lambda$1/2003749087.run(Unknown Source)
        at java.lang.Thread.run(Thread.java:748)

Found 1 deadlock.

```


### 3. 系统频繁full gc 导致系统卡顿原因分析 

todo 

### 4. 内存泄露到底是怎么回事？ 

todo 
   
### 5. JVM运行情况预估 

   用 jstat gc -pid 命令可以计算出如下一些关键数据，有了这些数据就可以采用之前介绍过的优化思路，先给自己的系统设置一些初始性的 JVM参数，比如堆内存大小，年轻代大小，Eden和Survivor的比例，老年代的大小，大对象的阈值，大龄对象进入老年代的阈值等。 

#### 5.1 年轻代对象增长的速率 
   
   可以执行命令 jstat -gc pid 1000 10 (每隔1秒执行1次命令，共执行10次)，通过观察EU(eden区的使用)来估算每秒eden大概新增多少对 象，如果系统负载不高，可以把频率1秒换成1分钟，甚至10分钟来观察整体情况。注意，一般系统可能有高峰期和日常期，所以需要在不 同的时间分别估算不同情况下对象增长速率。
   
#### 5.2 Young GC的触发频率和每次耗时 
   
   知道年轻代对象增长速率我们就能推根据eden区的大小推算出Young GC大概多久触发一次，Young GC的平均耗时可以通过 YGCT/YGC 公式算出，根据结果我们大概就能知道系统大概多久会因为Young GC的执行而卡顿多久。

#### 5.3 每次Young GC后有多少对象存活和进入老年代 
   
   这个因为之前已经大概知道Young GC的频率，假设是每5分钟一次，那么可以执行命令 jstat -gc pid 300000 10 ，观察每次结果eden， survivor和老年代使用的变化情况，在每次gc后eden区使用一般会大幅减少，survivor和老年代都有可能增长，这些增长的对象就是每次 Young GC后存活的对象，同时还可以看出每次Young GC后进去老年代大概多少对象，从而可以推算出老年代对象增长速率。 

#### 5.4 Full GC的触发频率和每次耗时 

知道了老年代对象的增长速率就可以推算出Full GC的触发频率了，Full GC的每次耗时可以用公式 FGCT/FGC 计算得出。 

#### 5.5 优化思路
   
   其实简单来说就是尽量让每次Young GC后的存活对象小于Survivor区域的50%，都留存在年轻代里。尽量别让对象进入老年 代。尽量减少Full GC的频率，避免频繁Full GC对JVM性能的影响。
   



