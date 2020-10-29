### 副本
   
   讲了多么多nosql数据库，一些概念的东西就不写不讲了
#### 搭建： 一台机器启动多个实例
   
   创建三个文件夹做为三个MongoDB实例的工作空间
   
```
mkdir /myMongoDb/mongodb1  -p 
mkdir /myMongoDb/mongodb2  -p 
mkdir /myMongoDb/mongodb3  -p
```
   
   在三个工作空间下分别创建数据，日志，进程存放等目录或者文件

```
mkdir /myMongoDb/mongodb1/data  -p 
mkdir /myMongoDb/mongodb1/log  -p 
mkdir /myMongoDb/mongodb1/pid  -p 

mkdir /myMongoDb/mongodb2/data  -p 
mkdir /myMongoDb/mongodb2/log  -p 
mkdir /myMongoDb/mongodb2/pid  -p 

mkdir /myMongoDb/mongodb3/data  -p 
mkdir /myMongoDb/mongodb3/log  -p 
mkdir /myMongoDb/mongodb3/pid  -p 

touch /myMongoDb/mongodb1/log/mongodb.log
touch /myMongoDb/mongodb2/log/mongodb.log
touch /myMongoDb/mongodb3/log/mongodb.log
touch /myMongoDb/mongodb1/pid/mongodb.pid
touch /myMongoDb/mongodb2/pid/mongodb.pid
touch /myMongoDb/mongodb3/pid/mongodb.pid
```

   在每个工作空间下创建配置文件

```
vim /myMongoDb/mongodb1/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /myMongoDb/mongodb1/log/mongodb.log

storage:
  dbPath: /myMongoDb/mongodb1/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /myMongoDb/mongodb1/pid/mongodb.pid

net:
  port: 7000
  bindIp: 192.168.204.199

replication:
  replSetName: rs0
  enableMajorityReadConcern: true
  
  
enableMajorityReadConcern:是否开启 readConcern 的级别为 “majority”，默认为 false；只有开启此选项，才能在 read 操作中使用 “majority”
destination:日志输出目的地，可以指定为 “file” 或者“syslog”，表述输出到日志文件，如果不指定，则会输出到标准输出中
journal:是否开启 journal 日志持久存储，journal 日志用来数据恢复，是 mongod 最基础的特性，通常用于故障恢复。64 位系统默认为 true，32 位默认为 false，建议开启，仅对 mongod 进程有效。
```

```
vim /myMongoDb/mongodb2/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /myMongoDb/mongodb2/log/mongodb.log

storage:
  dbPath: /myMongoDb/mongodb2/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /myMongoDb/mongodb2/pid/mongodb.pid

net:
  port: 7001
  bindIp: 192.168.204.199

replication:
  replSetName: rs0
  enableMajorityReadConcern: true
```

```
vim /myMongoDb/mongodb3/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /myMongoDb/mongodb3/log/mongodb.log

storage:
  dbPath: /myMongoDb/mongodb3/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /myMongoDb/mongodb3/pid/mongodb.pid
  
net:
  port: 7002
  bindIp: 192.168.204.199

replication:
  replSetName: rs0
  enableMajorityReadConcern: true
```
   
   注意：如果副本集的任何有投票权的成员使用内存存储引擎，则必须设置 writeConcernMajorityJournalDefault为false。如果副本集的任何有投票权的成员使用内存存储引擎且 
   writeConcernMajorityJournalDefault为true，则 "majority"写操作可能会失败。
   
   启动：
   
```
/usr/local/mongodb/bin/mongod -f /myMongoDb/mongodb1/mongodb.conf
/usr/local/mongodb/bin/mongod -f /myMongoDb/mongodb2/mongodb.conf
/usr/local/mongodb/bin/mongod -f /myMongoDb/mongodb3/mongodb.conf
```

   客户端连接，初始化副本：
   
```
mongo --host=192.168.204.188 --port=7000

> rs.initiate()
{
        "info2" : "no configuration specified. Using a default configuration for the set",
        "me" : "192.168.204.199:7000",
        "ok" : 1,
        "$clusterTime" : {
                "clusterTime" : Timestamp(1584452776, 1),
                "signature" : {
                        "hash" : BinData(0,"AAAAAAAAAAAAAAAAAAAAAAAAAAA="),
                        "keyId" : NumberLong(0)
                }
        },
        "operationTime" : Timestamp(1584452776, 1)
}
rs0:OTHER> 
rs0:PRIMARY> 

OK为1的话 则初始化成功
rs.conf()  查看副本信息
```
   
   加入副本和仲裁节点
   
```
rs.add("192.168.204.199:7001")  加入副本节点
rs.addArb("192.168.204.199:7002") 加入仲裁节点
```
   
   测试：
   
   现在连接的是主节点：
   
```
use taibai
db.xyj.insert({name:"猪八戒",age:28,gender:"男"});
```
   
   连接副本节点：
 
```
mongo --host=192.168.204.199 --port=7001
show dbs-----》  会报错

执行一下：rs.slaveOk() 命令
show dbs-----》  OK

rs.slaveOk()  默认  rs.slaveOk(true)   不想复制的时候可以执行rs.slaveOk(false)
```

  连接仲裁节点：

```
mongo --host=192.168.204.199 --port=7002
执行一下：rs.slaveOk() 命令
show dbs
返回
local  0.000GB

仲裁节点不会复制数据
```

#### 故障测试：
   
   副本挂掉的情况：
   	
   	  主节点和仲裁节点不受影响，如果副本挂掉的时候主节点还写入数据，当副本重启时主会同步数据给副本
   
   主节点挂掉的情况：
   	    
   	    执行故障转移，从节点成为主节点，仲裁节点没有选举权（不会主节点），当挂掉的主节点回来了。将成为副本
   
   主节点和仲裁节点挂掉的情况：
   	    
   	    副本还是副本，但是此时随便一个成员加入：
   	    仲裁节点加入：  副本成为主节点
   	    主节点加入：看谁的数据新
   
   副本节点和仲裁节点挂掉的情况：
   	    
   	    主节点降级为副本
   	    
#### 数据是如何复制的？
   
   当一个修改（增删改）操作到达主节点时，它对数据的操作经过特定的转化之后将被记录下来，这些称为oplog，
   
   从节点通过在主节点上打开一个tailable游标不断获取新进入主节点的oplog，并在自己的数据上回放。
   
#### 故障转移
   
   具有投票权的节点之间两两互相发送心跳
   
   当5次心跳未收到则判断为节点失联
   
   如果是主节点，从节点会发起选举，选出主节点
   
   如果失联的是从节点则不会发生新的选举
   
   选举基于RAFT一致性算法，选举成功的必要条件是大多数投票节点存活
   
   复制集中最多可以有50个节点，但最多只能有7个投票节点
   
   **影响选举的因素：**
   
   被选举为主节点的节点必须：
   
   能与多数节点建立连接
   
   具有较新的oplog
   
   根据配置文件的优先级
   
### 分片
   
   搭建两套副本
   
   创建shard文件夹，作为分片集群工作空间
   
   在shard创建replication1，作为第一个副本工作空间，然后创建三个文件夹做为三个MongoDB实例的工作空间
   
```
mkdir /shard/replication1/mongodb1  -p 
mkdir /shard/replication1/mongodb2  -p 
mkdir /shard/replication1/mongodb3  -p
```
   
   在三个工作空间下分别创建数据，日志，进程存放等目录或者文件
   
```
mkdir /shard/replication1/mongodb1/data  -p 
mkdir /shard/replication1/mongodb1/log  -p 
mkdir /shard/replication1/mongodb1/pid  -p 

mkdir /shard/replication1/mongodb2/data  -p 
mkdir /shard/replication1/mongodb2/log  -p 
mkdir /shard/replication1/mongodb2/pid  -p 

mkdir /shard/replication1/mongodb3/data  -p 
mkdir /shard/replication1/mongodb3/log  -p 
mkdir /shard/replication1/mongodb3/pid  -p 

touch /shard/replication1/mongodb1/log/mongodb.log
touch /shard/replication1/mongodb2/log/mongodb.log
touch /shard/replication1/mongodb3/log/mongodb.log

touch /shard/replication1/mongodb1/pid/mongodb.pid
touch /shard/replication1/mongodb2/pid/mongodb.pid
touch /shard/replication1/mongodb3/pid/mongodb.pid
```

   在每个工作空间下创建配置文件
   
```
vim /shard/replication1/mongodb1/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /shard/replication1/mongodb1/log/mongodb.log

storage:
  dbPath: /shard/replication1/mongodb1/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /shard/replication1/mongodb1/pid/mongodb.pid

net:
  port: 7000
  bindIp: 192.168.204.199

replication:
  replSetName: shard0
sharding:
  clusterRole: shardsvr
```
   
```
vim /shard/replication1/mongodb2/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /shard/replication1/mongodb2/log/mongodb.log

storage:
  dbPath: /shard/replication1/mongodb2/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /shard/replication1/mongodb2/pid/mongodb.pid

net:
  port: 7001
  bindIp: 192.168.204.199

replication:
  replSetName: shard0
sharding:
  clusterRole: shardsvr
```

   
```
vim /shard/replication1/mongodb3/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /shard/replication1/mongodb3/log/mongodb.log

storage:
  dbPath: /shard/replication1/mongodb3/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /shard/replication1/mongodb3/pid/mongodb.pid

net:
  port: 7002
  bindIp: 192.168.204.199

replication:
  replSetName: shard0
sharding:
  clusterRole: shardsvr
```

  启动：

```
/usr/local/mongodb/bin/mongod -f /shard/replication1/mongodb1/mongodb.conf
/usr/local/mongodb/bin/mongod -f /shard/replication1/mongodb2/mongodb.conf
/usr/local/mongodb/bin/mongod -f /shard/replication1/mongodb3/mongodb.conf
```
   
   在shard创建replication2，作为第二个副本工作空间，然后创建三个文件夹做为三个MongoDB实例的工作空间
   
```
mkdir /shard/replication2/mongodb1  -p 
mkdir /shard/replication2/mongodb2  -p 
mkdir /shard/replication2/mongodb3  -p
```
   
   在三个工作空间下分别创建数据，日志，进程存放等目录或者文件
   
```
mkdir /shard/replication2/mongodb1/data  -p 
mkdir /shard/replication2/mongodb1/log  -p 
mkdir /shard/replication2/mongodb1/pid  -p 

mkdir /shard/replication2/mongodb2/data  -p 
mkdir /shard/replication2/mongodb2/log  -p 
mkdir /shard/replication2/mongodb2/pid  -p 

mkdir /shard/replication2/mongodb3/data  -p 
mkdir /shard/replication2/mongodb3/log  -p 
mkdir /shard/replication2/mongodb3/pid  -p 

touch /shard/replication2/mongodb1/log/mongodb.log
touch /shard/replication2/mongodb2/log/mongodb.log
touch /shard/replication2/mongodb3/log/mongodb.log

touch /shard/replication2/mongodb1/pid/mongodb.pid
touch /shard/replication2/mongodb2/pid/mongodb.pid
touch /shard/replication2/mongodb3/pid/mongodb.pid
```
   
   在每个工作空间下创建配置文件
   
```
vim /shard/replication2/mongodb1/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /shard/replication2/mongodb1/log/mongodb.log

storage:
  dbPath: /shard/replication2/mongodb1/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /shard/replication2/mongodb1/pid/mongodb.pid

net:
  port: 8000
  bindIp: 192.168.204.199

replication:
  replSetName: shard1
sharding:
  clusterRole: shardsvr
```
   
```
vim /shard/replication2/mongodb2/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /shard/replication2/mongodb2/log/mongodb.log

storage:
  dbPath: /shard/replication2/mongodb2/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /shard/replication2/mongodb2/pid/mongodb.pid

net:
  port: 8001
  bindIp: 192.168.204.199

replication:
  replSetName: shard1
sharding:
  clusterRole: shardsvr
```

```
vim /shard/replication2/mongodb3/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /shard/replication2/mongodb3/log/mongodb.log

storage:
  dbPath: /shard/replication2/mongodb3/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /shard/replication2/mongodb3/pid/mongodb.pid

net:
  port: 8002
  bindIp: 192.168.204.199

replication:
  replSetName: shard1
sharding:
  clusterRole: shardsvr
```

   启动：
   
```
/usr/local/mongodb/bin/mongod -f /shard/replication2/mongodb1/mongodb.conf
/usr/local/mongodb/bin/mongod -f /shard/replication2/mongodb2/mongodb.conf
/usr/local/mongodb/bin/mongod -f /shard/replication2/mongodb3/mongodb.conf
```
   
   在shard创建configservice，作为配置节点副本工作空间，然后创建三个文件夹做为三个MongoDB实例的工作空间
   
```
mkdir /shard/configservice/mongodb1  -p 
mkdir /shard/configservice/mongodb2  -p 
mkdir /shard/configservice/mongodb3  -p
```
   
   在三个工作空间下分别创建数据，日志，进程存放等目录或者文件
   
```
mkdir /shard/configservice/mongodb1/data  -p 
mkdir /shard/configservice/mongodb1/log  -p 
mkdir /shard/configservice/mongodb1/pid  -p 

mkdir /shard/configservice/mongodb2/data  -p 
mkdir /shard/configservice/mongodb2/log  -p 
mkdir /shard/configservice/mongodb2/pid  -p 

mkdir /shard/configservice/mongodb3/data  -p 
mkdir /shard/configservice/mongodb3/log  -p 
mkdir /shard/configservice/mongodb3/pid  -p 

touch /shard/configservice/mongodb1/log/mongodb.log
touch /shard/configservice/mongodb2/log/mongodb.log
touch /shard/configservice/mongodb3/log/mongodb.log

touch /shard/configservice/mongodb1/pid/mongodb.pid
touch /shard/configservice/mongodb2/pid/mongodb.pid
touch /shard/configservice/mongodb3/pid/mongodb.pid
```

   在每个工作空间下创建配置文件
   
```
vim /shard/configservice/mongodb1/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /shard/configservice/mongodb1/log/mongodb.log

storage:
  dbPath: /shard/configservice/mongodb1/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /shard/configservice/mongodb1/pid/mongodb.pid

net:
  port: 9000
  bindIp: 192.168.204.199

replication:
  replSetName: shard2
sharding:
  clusterRole: configsvr
```

```
vim /shard/configservice/mongodb2/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /shard/configservice/mongodb2/log/mongodb.log

storage:
  dbPath: /shard/configservice/mongodb2/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /shard/configservice/mongodb2/pid/mongodb.pid

net:
  port: 9001
  bindIp: 192.168.204.199

replication:
  replSetName: shard2
sharding:
  clusterRole: configsvr
```

```
vim /shard/configservice/mongodb3/mongodb.conf
写入
systemLog:
  destination: file
  logAppend: true
  path: /shard/configservice/mongodb3/log/mongodb.log

storage:
  dbPath: /shard/configservice/mongodb3/data
  journal:
    enabled: true

processManagement:
  fork: true
  pidFilePath: /shard/configservice/mongodb3/pid/mongodb.pid

net:
  port: 9002
  bindIp: 192.168.204.199

replication:
  replSetName: shard2
sharding:
  clusterRole: configsvr
```

   1、configsvr：此实例为 config server，此实例默认侦听 27019 端口
   
   2、shardsvr：此实例为 shard（分片），侦听 27018 端口
   
   启动：
   
```
/usr/local/mongodb/bin/mongod -f /shard/configservice/mongodb1/mongodb.conf
/usr/local/mongodb/bin/mongod -f /shard/configservice/mongodb2/mongodb.conf
/usr/local/mongodb/bin/mongod -f /shard/configservice/mongodb3/mongodb.conf
```
   
   客户端连接，初始化第一套副本：
 
```
mongo --host=192.168.204.188 --port=7000

> rs.initiate()
```
   
   加入副本和仲裁节点
 
```
rs.add("192.168.204.199:7001")  加入副本节点
rs.addArb("192.168.204.199:7002")
```
   
   客户端连接，初始化第二套副本：

```
mongo --host=192.168.204.199 --port=8000

> rs.initiate()
```
   
   加入副本和仲裁节点
   
```
rs.add("192.168.204.199:8001")  加入副本节点
rs.addArb("192.168.204.199:8002")
```
   
   客户端连接，初始化配置节点副本：

```
mongo --host=192.168.204.199 --port=9000

> rs.initiate()
```
   
   加入副本和仲裁节点

```
rs.add("192.168.204.199:9001")  加入副本节点
rs.add("192.168.204.199:9002")
```
   
   创建仲裁节点
   
   在shard下创建route文件夹，然后创建两个文件夹作为路由节点实例工作空间

```
mkdir /shard/route/mongodb1/log -p
mkdir /shard/route/mongodb1/pid -p

touch /shard/route/mongodb1/log/mongodb.log
touch /shard/route/mongodb1/pid/mongodb.pid
```

```
 vim /shard/route/mongodb1/mongos.conf
 写入
 systemLog:
  destination: file
  logAppend: true
  path: /shard/route/mongodb1/log/mongodb.log

processManagement:
  fork: true
  pidFilePath: /shard/route/mongodb1/pid/mongodb.pid

net:
  port: 6000
  bindIp: 192.168.204.199

sharding:
  configDB: shard2/192.168.204.188:9000,192.168.204.188:9001,192.168.204.188:9002   #连接配置节点
```

   启动

```
mongos -f /shard/route/mongodb1/mongos.conf
```
   
   连接路由节点，将副本加入进来
   
```
mongo --host=192.168.204.188 --port=6000

sh.addShard(ip:port)   单机
sh.addShard(副本名:/ip:端口,ip:端口....)   副本

sh.addShard("shard0/192.168.204.199:7000,192.168.204.199:7001,192.168.204.199:7002")
sh.addShard("shard1/192.168.204.199:8000,192.168.204.199:8001,192.168.204.199:8002")

sh.status()  查看
```
   
   初始化分片
   
```
sh.enableSharding("数据库名")   //开启此数据库分片
sh.shardCollection("数据库名.集合名",{"根据此集合中那个字段进行分片":"分片规则是什么"})

sh.enableSharding("taibai")  
sh.shardCollection("taibai.xyj",{"likename":"hashed"})   hash
sh.shardCollection("taibai.sg",{"age":1})   范围
```
   
   测试分片：
   
   注意：必要要在路由节点上执行
   
```
测试hash
use taibai
for(var i=1;i<=1000;i++){db.xyj.insert({_id:i+"",likename:"taibai"+i})}  插入1000条数据
登录7000和8000两个副本查看
7000  ---》  502
8000  ---》  498

登录7000的副本7001，执行rs.slaveOk()命令之后，可能看到数据也同步过来了

测试范围
在做范围测试之前，由于范围默认是写入某个库，直到这个库当中这个集合的数据达到64M之后才会进行分片，然而我们没有这么大的数据量，所有将64M设置为1M
use config
db.settings.save({_id:"chunksize",value: 1})

use taibai
for(var i=1;i<=20000;i++){db.sg.save({"name":"fwefaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+i,"age":NumberInt(i%2000)})}
登录7000和8000两个副本查看
7000  ---》  7940
8000  ---》  16782
总数据：24722
```
   
   搭建多个路由节点：
   
   步骤和上面完全一样。。。。。
   
