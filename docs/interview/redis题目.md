### redis 题目

#### 你们系统中分布式锁是如何实现的？
   
   采用 redis 实现，setnx +  expire  
   
#### 上面使用 redis 实现分布式锁会存在什么问题？

   实际上上面的步骤是有问题的，setnx和expire是分开的两步操作，不具有原子性，如果执行完第一条指令应用异常或者重启了，锁将无法过期。
   
   改善:  
       
       使用Lua脚本（包含setnx和expire两条指令） 
       
       使用 set key value [EX seconds][PX milliseconds][NX|XX] 命令 (正确做法)
       
   再改善: 
       
       使用 set key value [EX seconds][PX milliseconds][NX|XX] 命令 看上去很OK，实际上在Redis集群的时候也会出现问题，
       比如说A客户端在Redis的master节点上拿到了锁，但是这个加锁的key还没有同步到slave节点，master故障，发生故障转移，
       一个slave节点升级为master节点，B客户端也可以获取同个key的锁，但客户端A也已经拿到锁了，这就导致多个客户端都拿到锁。
       
       所以针对Redis集群这种情况，还有其他方案 (Redlock算法 与 Redisson 实现)
       
        
       
   
#### 项目中除了使用 redis 实现分布式锁，还有哪些使用场景？
    
  ![](interview.assets/分布式锁.png)
    
#### redis 的数据持久化机制？如何保证数据不丢失？当 redis 内存满了以后，内存的淘汰策略？

#### Redis 主从复制的原理

#### redis 实现分布式锁的原理？redis 的分布式锁有什么问题？lua 脚本熟悉吗？

#### redis 中 key 的过期策略是什么？

#### redis 的持久化机制？AOF 和 RDB 的区别？

#### 什么是缓存击穿、缓存穿透、缓存雪崩？如何处理？

#### redis 持久化的机制？如何保证数据不丢失？

#### redis 的内存淘汰策略？key 的过期策略？

#### 使用 redis 过程中遇到过什么问题？如何解决热 key 问题？

#### 你们的 redis 使用的那种模式？集群模式和哨兵模式的区别？集群模式和哨兵模式如何保证 redis 集群的高可用？redis 集群的故障转移过程？

#### redis 集群如何实现扩容？

#### redis 的 rehash 的过程？

#### Redis 有哪些数据类型？List 中数据非常多怎么办？

#### Redis 的持久化机制？AOF 和 RDB 的优缺点？

#### Redis 的高可用怎么保证？线上有多少台机器？怎么部署的？

#### Redis 实现分布式锁的原理？存在什么问题？


#### Redis 中遇到热 key 会造成什么问题？如何发现热 key？如何解决热 key 的问题？
