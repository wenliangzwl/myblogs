### redis 题目

#### 什么是redis ? 

  高性能非关系型的键值对数据库
  
#### redis 为什么这么快? 
   
   1. 完全基于内存
   
   2. 数据结构简单，对数据操作也简单。
   
   3. 采用单线程，避免了不必要的上下文切换
   
   4. 使用多路I/O 复用模型，非阻塞I/O
   
#### redis 的应用场景
    
   1. 计数器
   
   2. 缓存
   
   3. 回话缓存
   
   4. 消息队列
   
   5. 分布式锁

    
#### 你们系统中分布式锁是如何实现的？
   
   采用 redis 实现，setnx +  expire  
   
#### 上面使用 redis 实现分布式锁会存在什么问题？

   实际上上面的步骤是有问题的，setnx和expire是分开的两步操作，不具有原子性，如果执行完第一条指令应用异常或者重启了，锁将无法过期。
   
   改善:  
       
       使用Lua脚本（包含setnx和expire两条指令） 
       
       使用 set key value [EX seconds][PX milliseconds][NX|XX] 命令 (正确做法)
       
   再改善: 
       
       使用 set key value [EX seconds][PX milliseconds][NX|XX] 命令 看上去很OK，实际上在Redis集群的时候也会出现问题，

         EX seconds: 设定过期时间，单位为秒
         PX milliseconds: 设定过期时间，单位为毫秒
         NX: 仅当key不存在时设置值
         XX: 仅当key存在时设置值
         
         public boolean tryLock_with_set(String key, String UniqueId, int seconds) {
            return "OK".equals(jedis.set(key, UniqueId, "NX", "EX", seconds));
         }

       比如说A客户端在Redis的master节点上拿到了锁，但是这个加锁的key还没有同步到slave节点，master故障，发生故障转移，
       一个slave节点升级为master节点，B客户端也可以获取同个key的锁，但客户端A也已经拿到锁了，这就导致多个客户端都拿到锁。
       
       所以针对Redis集群这种情况，还有其他方案 (Redlock算法 与 Redisson 实现)
       
        
       
   
#### 项目中除了使用 redis 实现分布式锁，还有哪些使用场景？
    
  ![](interview.assets/分布式锁.png)
    
#### 如何保证数据不丢失？当 redis 内存满了以后，内存的淘汰策略？
   
   RDB:  产生一个数据快照文件
   
   AOF:  实时追加命令的日志文件
   
   redis 内存满了，使用 redis的内存淘汰机制。
   
   淘汰策略:
    
    noeviction(默认策略)：若是内存的大小达到阀值的时候，所有申请内存的指令都会报错。
    
    allkeys-lru：所有key都是使用LRU(最近最少使用)算法进行淘汰。
    
    volatile-lru：所有设置了过期时间的key使用LRU算法进行淘汰。
    
    allkeys-random：所有的key使用随机淘汰的方式进行淘汰。
    
    volatile-random：所有设置了过期时间的key使用随机淘汰的方式进行淘汰。
    
    volatile-ttl：所有设置了过期时间的key根据过期时间进行淘汰，越早过期就越快被淘汰。
    
    
   
#### Redis 主从复制的原理

#### redis 中 key 的过期策略是什么？

   定时删除：创建一个定时器，定时的执行对key的删除操作。
       
   惰性删除：每次只有再访问key的时候，才会检查key的过期时间，若是已经过期了就执行删除。
       
   定期删除：每隔一段时间，就会检查删除掉过期的key。

#### redis 的持久化机制？AOF 和 RDB 的区别？

   RDB:  产生一个数据快照文件
    
    rdb 是redis 默认的持久化方式，按照一定的时间将内存的数据以快照的形式保存到硬盘中，产生dump.rdb 
         
      优点: 只有一个文件，方便持久化，容灾性好，性能最大化，fork 子进程来完成写操作， 相对于数据集大时，比AOF 的启动效率更高.
      
      缺点:  数据安全性低,rdb 是间隔一段时间进行持久化，如果持久化之间redis发生故障，会发生数据丢失
      
    AOF:  实时追加命令的日志文件， 将redis 执行的每次写命令记录到单独的日志文件里，当两种方式同时开启，数据恢复会优先选择AOF恢复
    
       优点： 数据安全，通过append  

#### 什么是缓存击穿、缓存穿透、缓存雪崩？如何处理？

#### 使用 redis 过程中遇到过什么问题？如何解决热 key 问题？

#### 你们的 redis 使用的那种模式？集群模式和哨兵模式的区别？集群模式和哨兵模式如何保证 redis 集群的高可用？redis 集群的故障转移过程？

#### redis 集群如何实现扩容？

#### redis 的 rehash 的过程？

#### Redis 有哪些数据类型？List 中数据非常多怎么办？

  string、 list、 hash、set、zset 

  ![](interview.assets/redis五种数据结构.png)

   
   string 应用场景

      计数器 （INCR article:readcount:{文章id}  GET article:readcount:{文章id} ）
      Web集群session共享（spring session + redis实现session共享）
      分布式系统全局序列号	（INCRBY  orderId  1000		//redis批量生成序列号提升性能）

   Hash应用场景

      对象缓存（HMSET  user  {userId}:name  zhuge  {userId}:balance  1888）
      电商购物车 （1）以用户id为key （2）商品id为field （3）商品数量为value
        （添加商品hset cart:1001 10088 1
         增加数量hincrby cart:1001 10088 1
         商品总数hlen cart:1001
         删除商品hdel cart:1001 10088
         获取购物车所有商品hgetall cart:1001）  
   
   List应用场景

      常用数据结构：
      Stack(栈) = LPUSH + LPOP
      Queue(队列）= LPUSH + RPOP
      Blocking MQ(阻塞队列）= LPUSH + BRPOP

      应用场景：
      1. 微博消息和微信公号消息
      2. 微博和微信公号消息流

   Set应用场景

      1. 微信抽奖小程序
         1）点击参与抽奖加入集合
            SADD key {userlD}
         2）查看参与抽奖所有用户
            SMEMBERS key	  
         3）抽取count名中奖者
            SRANDMEMBER key [count] / SPOP key [count]

      2. 微信微博点赞，收藏，标签
         1) 点赞
            SADD  like:{消息ID}  {用户ID}
         2) 取消点赞
            SREM like:{消息ID}  {用户ID}
         3) 检查用户是否点过赞
            SISMEMBER  like:{消息ID}  {用户ID}
         4) 获取点赞的用户列表
            SMEMBERS like:{消息ID}
         1) 获取点赞用户数 
            SCARD like:{消息ID}

   Zset集合操作实现排行榜

      1）点击新闻
         ZINCRBY  hotNews:20190819  1  守护香港
      2）展示当日排行前十
         ZREVRANGE  hotNews:20190819  0  9  WITHSCORES 
      3）七日搜索榜单计算
         ZUNIONSTORE  hotNews:20190813-20190819  7 hotNews:20190813  hotNews:20190814.. hotNews:20190819
      4）展示七日排行前十
         ZREVRANGE hotNews:20190813-20190819  0  9  WITHSCORES


#### Redis 的持久化机制？AOF 和 RDB 的优缺点？

#### Redis 的高可用怎么保证？线上有多少台机器？怎么部署的？

#### Redis 实现分布式锁的原理？存在什么问题？


#### Redis 中遇到热 key 会造成什么问题？如何发现热 key？如何解决热 key 的问题？

   热key 遇到问题
      
      当前key是一个热点key（例如一个热门的娱乐新闻），并发量非常大。 重建缓存不能在短时间完成， 可能是一个复杂计算， 例如复杂的SQL、 多次IO、 多个依赖等。
   
   解决热key

      要解决这个问题主要就是要避免大量线程同时重建缓存。 我们可以利用互斥锁来解决，此方法只允许一个线程重建缓存， 其他线程等待重建缓存的线程执行完， 重新从 缓存获取数据即可。