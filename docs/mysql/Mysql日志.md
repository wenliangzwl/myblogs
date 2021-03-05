### Mysql 日志 

#### binlog 日志
   
   binlog日志有三种记录模式并各有优缺点：
   
   ![](mysql.assets/binlog日志记录模式.png)
   
   MySQL默认的binlog记录模式为row。
   
#### redo log 日志   （重做日志）   
   
   MySQL 在更新数据时，为了减少磁盘的随机 IO，因此并不会直接更新磁盘上的数据，而是先更新 Buffer Pool 中缓存页的数据，等到合适的时间点，再将这个缓存页持久化到磁盘。
   而 Buffer Pool 中所有缓存页都是处于内存当中的，当 MySQL 宕机或者机器断电，内存中的数据就会丢失，因此 MySQL 为了防止缓存页中的数据在更新后出现数据丢失的现象，引入了 redo log 机制。
   
   当进行增删改操作时，MySQL 会在更新 Buffer Pool 中的缓存页数据时，会记录一条对应操作的 redo log 日志，这样如果出现 MySQL 宕机或者断电时，如果有缓存页的数据还没来得及刷入磁盘，
   那么当 MySQL 重新启动时，可以根据 redo log 日志文件，进行数据重做，将数据恢复到宕机或者断电前的状态，保证了更新的数据不丢失，因此 redo log 又叫做重做日志。它的本质是保证事务提交后，更新的数据不丢失。
   
   与 binlog 不同的是，redo log 中记录的是*物理日志*，是 InnoDB 引擎记录的，而 binlog 记录的是*逻辑日志*，是 MySQL 的 Server 层记录的。
   什么意思呢？binlog 中记录的是 SQL 语句（实际上并不一定为 SQL 语句，这与 binlog 的格式有关，如果指定的是 STATEMENT 格式，那么 binlog 中记录的就是 SQL 语句），也就是逻辑日志；
   而 redo log 中则记录的是对磁盘上的某个表空间的某个数据页的某一行数据的某个字段做了修改，修改后的值为多少，它记录的是对物理磁盘上数据的修改，因此称之为物理日志。
   
#####  redo log buffer
   
   当一条 SQL 更新完 Buffer Pool 中的缓存页后，就会记录一条 redo log 日志，前面提到了 redo log 日志是存储在磁盘上的，那么此时是不是立马就将 redo log 日志写入磁盘呢？显然不是的，而是先写入一个叫做 redo log buffer 的缓存中，redo log buffer 是一块不同于 buffer pool 的内存缓存区，在 MySQL 启动的时候，向内存中申请的一块内存区域，它是 redo log 日志缓冲区，默认大小是 16MB，由参数 innodb_log_buffer_size 控制（前面的截图中可以看到）。
   
   redo log buffer 内部又可以划分为许多 redo log block，每个 redo log block 大小为 512 字节。我们写入的 redo log 日志，最终实际上是先写入在 redo log buffer 的 redo log block 中，然后在某一个合适的时间点，将这条 redo log 所在的 redo log block 刷入到磁盘中。
   