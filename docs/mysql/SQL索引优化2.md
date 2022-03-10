### 1. 分页查询优化

```mysql
select * from employees limit 10000, 10;
```

  表示从表 employees 中取出从 10001 行开始的 10 行记录。看似只查询了 10 条记录，实际这条 SQL 
  是先读取 10010 条记录，然后抛弃前 10000 条记录，然后读到后面 10 条想要的数据。
  因此要查询一张大表比较靠后的数据，执行效率 是非常低的。
  
#### 2.1 常见分页常见优化

  根据自增且连续主键排序的分页查询的例子:
  
```mysql
select * from employees limit 90000,5;
```

![](mysql.assets/索引示例2-1.png)

  该 SQL 表示查询从第 90001开始的五行数据，没添加单独 order by，表示通过主键排序。
  我们再看表 employees ，因 为主键是自增并且连续的，所以可以改写成按照主键去查询从第 90001开始的五行数据，
  如下:

```mysql
select * from employees where id > 90000 limit 5;
```

![](mysql.assets/索引示例2-2.png)

查询的结果是一致的。我们再对比一下执行计划:

```mysql
EXPLAIN select * from employees limit 90000,5;
```

![](mysql.assets/索引示例2-3.png)

```mysql
EXPLAIN select * from employees where id > 90000 limit 5;
```

![](mysql.assets/索引示例2-4.png)

  显然改写后的 SQL 走了索引，而且扫描的行数大大减少，执行效率更高。

  但是，这条改写的SQL 在很多场景并不实用，因为表中可能某些记录被删后，主键空缺，导致结果不一致，如下图试验
  所示(先删除一条前面的记录，然后再测试原 SQL 和优化后的 SQL):

```mysql
select * from employees limit 90000,5;
```

![](mysql.assets/索引示例2-5.png)

```mysql
select * from employees where id > 90000 limit 5;
```

![](mysql.assets/索引示例2-6.png)

  两条 SQL 的结果并不一样，因此，如果主键不连续，不能使用上面描述的优化方法。
  
  另外如果原 SQL 是 order by 非主键的字段，按照上面说的方法改写会导致两条 SQL 的结果不一致。所以这种改写得满 足以下两个条件:
        
    1. 主键自增且连续
    2. 结果是按照主键排序的

##### 2.2 根据非主键字段排序的分页查询

```mysql
select * from employees order by name limit 90000,5; 
```

![](mysql.assets/索引示例2-7.png)

```mysql
EXPLAIN select * from employees ORDER BY name limit 90000,5;
```

![](mysql.assets/索引示例2-8.png)

  发现并没有使用 name 字段的索引(key 字段对应的值为 null)。 扫描整个索引并查找到没索引 的行(可能要遍历多个索引树)的成本比扫描全表的成本更高，
  所以优化器放弃使用索引。

  知道不走索引的原因，那么怎么优化呢? 

   其实关键是让排序时返回的字段尽可能少，所以可以让排序和分页操作先查出主键，然后根据主键查到对应的记录，SQL 改写如下:
   
```mysql
select * from employees e inner join (select id from employees order by name limit 90000,5) ed on e.id = ed.id;
```

![](mysql.assets/索引示例2-9.png)

 需要的结果与原 SQL 一致，执行时间减少了一半以上，我们再对比优化前后sql的执行计划:

```mysql
explain select * from employees e inner join (select id from employees order by name limit 90000,5) ed on e.id = ed.id;
```

![](mysql.assets/索引示例2-10.png)

  原 SQL 使用的是 filesort 排序，而优化后的 SQL 使用的是索引排序。
  
### 2. Join关联查询优化

#### 2.1 示例表 

```mysql
CREATE TABLE `t1` (
 `id` int(11) NOT NULL AUTO_INCREMENT,
 `a` int(11) DEFAULT NULL,
 `b` int(11) DEFAULT NULL,
 PRIMARY KEY (`id`),
 KEY `idx_a` (`a`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 create table t2 like t1;
 
 ‐‐ 插入一些示例数据
‐‐ 往t1表插入1万行记录
drop procedure if exists insert_t1; 
delimiter ;;
create procedure insert_t1()
begin
   declare i int;
   set i=1;
   while(i<=10000)do
     insert into t1(a,b) values(i,i);
     set i=i+1;
   end while;
 end;;
 delimiter ;
  call insert_t1();
 
 ‐‐ 往t2表插入100行记录
drop procedure if exists insert_t2;
delimiter ;;
create procedure insert_t2()
begin
  declare i int;
  set i=1;
  while(i<=100)do
    insert into t2(a,b) values(i,i);
    set i=i+1;
  end while;
end;;
delimiter ;
 call insert_t2();
```

#### 2.2 mysql的表关联常见有两种算法 

    Nested-Loop Join 算法
    Block Nested-Loop Join 算法

##### 2.2.1 嵌套循环连接 Nested-Loop Join(NLJ) 算法

    一次一行循环地从第一张表(称为驱动表)中读取行，在这行数据中取到关联字段，根据关联字段在另一张表(被驱动
    表)里取出满足条件的行，然后取出两张表的结果合集。

```mysql
 EXPLAIN select * from t1 inner join t2 on t1.a= t2.a;
```

![](mysql.assets/索引示例2-11.png)

    从执行计划中可以看到这些信息:
        驱动表是 t2，被驱动表是 t1。先执行的就是驱动表(执行计划结果的id如果一样则按从上到下顺序执行sql);优 化器一般会优先选择小表做驱动表。
        所以使用 inner join 时，排在前面的表并不一定就是驱动表。
        当使用left join时，左表是驱动表，右表是被驱动表，当使用right join时，右表时驱动表，左表是被驱动表， 
        当使用join时，mysql会选择数据量比较小的表作为驱动表，大表作为被驱动表。
        使用了 NLJ算法。一般 join 语句中，如果执行计划 Extra 中未出现 Using join buffer 则表示使用的 join 算 法是 NLJ。
    
    上面sql的大致流程如下:
        1. 从表 t2 中读取一行数据(如果t2表有查询过滤条件的，会从过滤结果里取出一行数据); 
        2. 从第 1 步的数据中，取出关联字段 a，到表 t1 中查找;
        3. 取出表 t1 中满足条件的行，跟 t2 中获取到的结果合并，作为结果返回给客户端;
        4. 重复上面 3 步。

    整个过程会读取 t2 表的所有数据(扫描100行)，然后遍历这每行数据中字段 a 的值，根据 t2 表中 a 的值索引扫描 t1 表 中的对应行
    (扫描100次 t1 表的索引，1次扫描可以认为最终只扫描 t1 表一行完整数据，也就是总共 t1 表也扫描了100 行)。因此整个过程扫描了 200 行。 
    如果被驱动表的关联字段没索引，使用NLJ算法性能会比较低(下面有详细解释)，mysql会选择Block Nested-Loop Join 算法。

##### 2.2.2 基于块的嵌套循环连接 Block Nested-Loop Join(BNL)算法

   把驱动表的数据读入到 join_buffer 中，然后扫描被驱动表，把被驱动表每一行取出来跟 join_buffer 中的数据做对比。

```mysql
 EXPLAIN select * from t1 inner join t2 on t1.b= t2.b;
```

![](mysql.assets/索引示例2-12.png)

  Extra 中 的Using join buffer (Block Nested Loop)说明该关联查询使用的是 BNL 算法。

  上面sql的大致流程如下:
    
    1. 把 t2 的所有数据放入到 join_buffer 中
    2. 把表 t1 中每一行取出来，跟 join_buffer 中的数据做对比 
    3. 返回满足 join 条件的数据
   
    整个过程对表 t1 和 t2 都做了一次全表扫描，因此扫描的总行数为10000(表 t1 的数据总量) + 100(表 t2 的数据总量) = 10100。
    并且 join_buffer 里的数据是无序的，因此对表 t1 中的每一行，都要做 100 次判断，所以内存中的判断次数是 100 * 10000= 100 万次。
   
    这个例子里表 t2 才 100 行，要是表 t2 是一个大表，join_buffer 放不下怎么办呢?
    
    join_buffer 的大小是由参数 join_buffer_size 设定的，默认值是 256k。如果放不下表 t2 的所有数据话，策略很简单， 就是分段放。
    
    比如 t2 表有1000行记录， join_buffer 一次只能放800行数据，那么执行过程就是先往 join_buffer 里放800行记录，
    然后从 t1 表里取数据跟 join_buffer 中数据对比得到部分结果，然后清空 join_buffer ，再放入 t2 表剩余200行记录，
    再 次从 t1 表里取数据跟 join_buffer 中数据对比。所以就多扫了一次 t1 表。

  被驱动表的关联字段没索引为什么要选择使用 BNL 算法而不使用 Nested-Loop Join 呢?
    
    如果上面第二条sql使用 Nested-Loop Join，那么扫描行数为 100 * 10000 = 100万次，这个是磁盘扫描。 
    很显然，用BNL磁盘扫描次数少很多，相比于磁盘扫描，BNL的内存计算会快得多。 因此MySQL对于被驱动表的关联字段没索引的关联查询，
    一般都会使用 BNL 算法。如果有索引一般选择 NLJ 算法，有 索引的情况下 NLJ 算法比 BNL算法性能更高。

##### 2.2.3 对于关联sql的优化

    关联字段加索引，让mysql做join操作时尽量选择NLJ算法 
    小表驱动大表，写多表连接sql时如果明确知道哪张表是小表可以用straight_join写法固定连接驱动方式，省去mysql优化器自己判断的时间
    
    
   straight_join解释:
   
    straight_join功能同join类似，但能让左边的表来驱动右边的表，能改表优化器对于联表查询的执 行顺序。
    比如:select * from t2 straight_join t1 on t2.a = t1.a; 代表指定mysql选着 t2 表作为驱动表。
        straight_join只适用于inner join，并不适用于left join，right join。(因为left join，right join已经代表指 定了表的执行顺序)
        尽可能让优化器去判断，因为大部分情况下mysql优化器是比人要聪明的。使用straight_join一定要慎重，因 为部分情况下人为指定的执行顺序并不一定会比优化引擎要靠谱。

  对于小表定义的明确

    在决定哪个表做驱动表的时候，应该是两个表按照各自的条件过滤，过滤完成之后，计算参与 join 的各个字段的总数据 量，数据量小的那个表，就是“小表”，
    应该作为驱动表。

### 3. in和exsits优化

  原则:小表驱动大表，即小的数据集驱动大的数据集
  
#### 3.1 in:当t2表的数据集小于t1表的数据集时，in优于exists

```mysql
select * from t1 where id in (select id from t2)
```

```sql
### 等价于:
for(select id from t2){
    select * from t1 where t1.id = t2.id
}
```

#### 3.2 exists:当t1表的数据集小于t2表的数据集时，exists优于in

  将主查询A的数据，放到子查询B中做条件验证，根据验证结果(true或false)来决定主查询的数据是否保留

```mysql
select * from t2 where exists (select 1 from t1 where t1.id = t2.id)
```

```sql
 #等价于:
for(select * from t2){
    select * from t1 where t1.id = t2.id
}

#t2表与t1表的ID字段应建立索引
```

    1、EXISTS (subquery)只返回TRUE或FALSE,因此子查询中的SELECT * 也可以用SELECT 1替换,官方说法是实际执行时会 忽略SELECT清单,因此没有区别
    2、EXISTS子查询的实际执行过程可能经过了优化而不是我们理解上的逐条对比 
    3、EXISTS子查询往往也可以用JOIN来代替，何种最优需要具体问题具体分析

### 4. count(*)查询优化

```mysql
‐‐ 临时关闭mysql查询缓存，为了查看sql多次执行的真实时间
set global query_cache_size=0;
set global query_cache_type=0;
EXPLAIN select count(1) from employees;
EXPLAIN select count(id) from employees;
EXPLAIN select count(name) from employees;
EXPLAIN select count(*) from employees;
```

  注意:以上4条sql只有根据某个字段count不会统计字段为null值的数据行

![](mysql.assets/索引示例2-13.png)

    四个sql的执行计划一样，说明这四个sql执行效率应该差不多
        字段有索引:count(*)≈count(1)>count(字段)>count(主键 id) //字段有索引，count(字段)统计走二级索引，二 级索引存储数据比主键索引少，所以count(字段)>count(主键 id)
        字段无索引:count(*)≈count(1)>count(主键 id)>count(字段) //字段没有索引count(字段)统计走不了索引， count(主键 id)还可以走主键索引，所以count(主键 id)>count(字段) 

        count(1)跟count(字段)执行过程类似，不过count(1)不需要取出字段统计，就用常量1做统计，count(字段)还需要取出 字段，所以理论上count(1)比count(字段)会快一点。
        
        count(*) 是例外，mysql并不会把全部字段取出来，而是专门做了优化，不取值，按行累加，效率很高，所以不需要用 count(列名)或count(常量)来替代 count(*)。

        为什么对于count(id)，mysql最终选择辅助索引而不是主键聚集索引?因为二级索引相对主键索引存储数据更少，检索 性能应该更高，mysql内部做了点优化(应该是在5.7版本才优化)。

#### 4.1 常见优化方法

##### 4.1.1 查询mysql自己维护的总行数 

    对于myisam存储引擎的表做不带where条件的count查询性能是很高的，因为myisam存储引擎的表的总行数会被 mysql存储在磁盘上，查询不需要计算

```mysql
explain select count(*) from test_myisam;
```

![](mysql.assets/索引示例2-14.png)

    对于innodb存储引擎的表mysql不会存储表的总记录行数(因为有MVCC机制)，查询count需要实时计算

##### 4.1.2  show table status

  如果只需要知道表总行数的估计值可以用如下sql查询，性能很高

```mysql
show table status like 'employees';
```

![img.png](mysql.assets/索引示例2-15.png)

##### 4.1.3 将总数维护到Redis里

  插入或删除表数据行的时候同时维护redis里的表总行数key的计数值(用incr或decr命令)，但是这种方式可能不准，很难 保证表操作和redis操作的事务一致性

##### 4.1.4 增加数据库计数表
  
   插入或删除表数据行的时候同时维护计数表，让他们在同一个事务里操作


### 5. 阿里巴巴Mysql规范解读

![img.png](mysql.assets/索引优化2-16.png)

![img.png](mysql.assets/索引优化2-17.png)

![img.png](mysql.assets/索引优化2-18.png)

![img.png](mysql.assets/索引优化2-19.png)







