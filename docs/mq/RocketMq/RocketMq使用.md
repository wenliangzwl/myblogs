### rocketmq的物理部署结构
   
   ![](RocketMq.assets/rocketmq物理部署结构图.png)
   
   
   Name Server是一个几乎无状态节点，可集群部署，节点之间无任何信息同步。
   
   Broker部署相对复杂，Broker分为Master与Slave，一个Master可以对应多个Slave，但是一个Slave只能对应一个Master，Master与Slave的对应关系通过指定相同的BrokerName，
   不同的BrokerId来定义，BrokerId为0表示Master，非0表示Slave。Master也可以部署多个。每个Broker与Name Server集群中的所有节点建立长连接，定时注册Topic信息到所有Name Server。
   
   Producer与Name Server集群中的其中一个节点（随机选择）建立长连接，定期从Name Server取Topic路由信息，并向提供Topic服务的Master建立长连接，
   且定时向Master发送心跳。Producer完全无状态，可集群部署。
   
### 支持的特性
   
   发布/订阅。
   
   优先级，支持队列优先级，数字表达。
   
   消息顺序的严格保证。
   
   消息过滤，Broker端过滤：支持类型，tag，语法表达式过滤；Consumer端过滤：自定义实现即可。
   
   持久化，充分利用linux系统内存cache提升性能。
   
   消息可靠性，支持异步，同步双写。
   
   低延迟，在消息不堆积情况下，消息到达Broker后，能立刻到达Consumer。RocketMQ使用长轮询Pull方式 长轮询详解，可保证消息非常实时，消息实时性不低于Push。
   
   每个消息必须消费并ack一次。
   
   队列持久化，定期删除某段时间之前的数据。
   
   回溯消费，支持往前，往后，按照时间，可达毫秒级别。
   
   消息堆积，
   
   分布式事务，根据offset更改msg状态。
   
   定时消息，支持级别，5s,5s,1m。
   
   消息重试，

### 架构图
   
   producer集群：拥有相同的producerGroup,一般来讲，Producer不必要有集群的概念，这里的集群仅仅在RocketMQ的分布式事务中有用到
   
   Name Server集群：提供topic的路由信息，路由信息数据存储在内存中，broker会定时的发送路由信息到nameserver中的每一个机器，来进行更新，所以name server集群可以简单理解为无状态
   （实际情况下可能存在每个nameserver机器上的数据有短暂的不一致现象，但是通过定时更新，大部分情况下都是一致的）
   
   broker集群：一个集群有一个统一的名字，即brokerClusterName，默认是DefaultCluster。一个集群下有多个master，每个master下有多个slave。master和slave算是一组，拥有相同的brokerName,
   不同的brokerId，master的brokerId是0，而slave则是大于0的值。master和slave之间可以进行同步复制或者是异步复制。
   
   consumer集群：拥有相同的consumerGroup。

#### 通信关系：
   
   ![](RocketMq.assets/rocketmq通信.png)

### 消息存储
   
   为提高消息读写并发能力，将一个topic消息进行拆分，kafka称为分区，rocketmq称为队列。
   
   对于kafka：为了防止一个分区的消息文件过大，会拆分成一个个固定大小的文件，所以一个分区就对应了一个目录。分区与分区之间是相互隔离的。
   
   对于RocketMQ：则是所有topic的数据混在一起进行存储，默认超过1G的话，则重新创建一个新的文件。消息的写入过程即写入该混杂的文件中，然后又有一个线程服务，在不断的读取分析该混杂文件，将消息进行分拣，
   然后存储在对应队列目录中（存储的是简要信息，如消息在混杂文件中的offset，消息大小等）
   
   所以RocketMQ需要2次寻找，第一次先找队列中的消息概要信息，拿到概要信息中的offset，根据这个offset再到混杂文件中找到想要的消息。而kafka则只需要直接读取分区中的文件即可得到想要的消息。

### producer端发现
   
   Producer端如何来发现新的broker地址。
   
   对于kafka来说：Producer端需要配置broker的列表地址，Producer也从一个broker中来更新broker列表地址（从中发现新加入的broker）。
   
   对于RocketMQ来说：Producer端需要Name Server的列表地址，同时还可以定时从一个HTTP地址中来获取最新的Name Server的列表地址，然后从其中的一台Name Server来获取全部的路由信息，从中发现新的broker。

### 消费offset的存储
   
   对于kafka：Consumer将消费的offset定时存储到ZooKeeper上，利用ZooKeeper保障了offset的高可用问题。
   
   对于RocketMQ:Consumer将消费的offset定时存储到broker所在的机器上，这个broker优先是master，如果master挂了的话，则会选择slave来存储，broker也是将这些offset定时刷新到本地磁盘上，
   同时slave会定时的访问master来获取这些offset。

### consumer负载均衡
    
   对于负载均衡，在出现分区或者队列增加或者减少的时候、Consumer增加或者减少的时候都会进行reblance(reblance相等于jvm 的full gc操作)操作。
    
   对于RocketMQ:客户端自己会定时对所有的topic的进行reblance操作，对于每个topic，会从broker获取所有Consumer列表，从broker获取队列列表，按照负载均衡策略，计算各自负责哪些队列。
   这种就要求进行负载均衡的时候，各个Consumer获取的数据是一致的，不然不同的Consumer的reblance结果就不同。
    
   对于kafka：kafka之前也是客户端自己进行reblance，依靠ZooKeeper的监听，来监听上述2种情况的出现，一旦出现则进行reblance。现在的版本则将这个reblance操作转移到了broker端来做，
   不但解决了RocketMQ上述的问题，同时减轻了客户端的操作，是的客户端更加轻量级，减少了和其他语言集成的工作量。
    

### RocketMq消息存储
   
   RocketMQ采用了单一的日志文件，即把同1台机器上面所有topic的所有queue的消息，存放在一个文件里面，从而避免了随机的磁盘写入。
   
   ![](RocketMq.assets/消息存储.png)
   
   如上图所示，所有消息都存在一个单一的CommitLog文件里面，然后有后台线程异步的同步到ConsumeQueue，再由Consumer进行消费。
   
   这里至所以可以用“异步线程”，也是因为消息队列天生就是用来“缓冲消息”的。只要消息到了CommitLog，发送的消息也就不会丢。只要消息不丢，那就有了“充足的回旋余地”，用一个后台线程慢慢同步到ConsumeQueue，
   再由Consumer消费。
   
   可以说，这也是在消息队列内部的一个典型的“最终一致性”的案例：Producer发了消息，进了CommitLog，此时Consumer并不可见。但没关系，只要消息不丢，消息最终肯定会进入ConsumeQueue，让Consumer可见。
   

   
   