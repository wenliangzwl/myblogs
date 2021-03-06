### SynchronousQueue
    
   无缓冲阻塞队列，用来在两个线程之间移交元素
   模式相同则入栈（队），不同则出栈（队），所以并非真正的无缓冲
   队列为空也入栈（队）
   并不是真正的队列，不维护存储空间，维护的是一组线程，这些线程在等待着放入或者移出元素
   这种阻塞队列确实是非常复杂的，但是却非常有用。SynchronousQueue是一种极为特殊的阻塞队列，它没有实际的容量，任意线程（生产者线程或者消费者线程，生产类型的操作比如put，offer，消费类型的操作比如poll，take）
   都会等待知道获得数据或者交付完成数据才会返回，一个生产者线程的使命是将线程附着着的数据交付给一个消费者线程，而一个消费者线程则是等待一个生产者线程的数据。它们会在匹配到互斥线程的时候就会做数据交易，
   比如生产者线程遇到消费者线程时，或者消费者线程遇到生产者线程时，一个生产者线程就会将数据交付给消费者线程，然后共同退出。在java线程池newCachedThreadPool中就使用了这种阻塞队列。
   
#### 优点
    
   将更多关于任务状态的信息反馈给生产者。当交付被接受时，它就知道消费者已经得到了任务，而不是简单地把任务放入一个队列——这种区别就好比将文件直接交给同事，
   还是将文件放到她的邮箱中并希望她能尽快拿到文件。
   
### ArrayBlockingQueue
    
   由数组支持的有界阻塞队列。此队列对元素按 FIFO（先进先出）进行排序。队首是已在队列中最长时间的元素。队尾是最短时间出现在队列中的元素。新元素插入到队列的尾部，并且队列检索操作在队列的开头获取元素。
   这是经典的“有界缓冲区”，其中固定大小的数组包含由生产者插入并由消费者提取的元素。一旦创建，容量将无法更改。试图将一个元素放入一个完整的队列将导致操作阻塞；从空队列中取出一个元素的尝试也会类似地阻塞。
   
   此类支持可选的公平性策略，用于排序正在等待的生产者和使用者线程。默认情况下，不保证此排序。但是，将公平性设置为true构造的队列将按FIFO顺序授予线程访问权限。公平通常会降低吞吐量，但会减少可变性并避免饥饿。

#### 缺点
   
   a）队列长度固定且必须在初始化时指定，所以使用之前一定要慎重考虑好容量；
   b）如果消费速度跟不上入队速度，则会导致提供者线程一直阻塞，且越阻塞越多，非常危险；
   c）只使用了一个锁来控制入队出队，效率较低。

### LinkedBlockingQueue
   
   LinkedBlockingQueue - 单链表实现的阻塞队列。该队列按 FIFO（先进先出）排序元素，新元素从队列尾部插入，从队首获取元素.是深入并发编程的基础数据结构.
   
   以单链表实现的阻塞队列，它是线程安全的，借助分段的思想把入队出队分裂成两个锁

### ConcurrentLinkedQueue
   
   基于链表实现的无界线程安全队列
   只实现了Queue接口，并没有实现BlockingQueue接口，所以它不是阻塞队列，也不能用于线程池中，但是它是线程安全的，可用于多线程环境中。无界队列

### DelayQueue

   DelayQueue实现了BlockingQueue，所以它是一个阻塞队列。
   另外，DelayQueue还组合了一个叫做Delayed的接口，DelayQueue中存储的所有元素必须实现Delayed接口。

#### java中的线程池实现定时任务是直接用的DelayQueue吗？
        
    不是，ScheduledThreadPoolExecutor中使用的是它自己定义的内部类DelayedWorkQueue，其实里面的实现逻辑基本都是一样的，只不过DelayedWorkQueue里面没有使用现成的PriorityQueue，
    而是使用数组又实现了一遍优先级队列，本质上没有什么区别。

### PriorityBlockingQueue
   
   底层是数组,平衡二叉树堆的实现

### PriorityQueue
   
   优先级队列：一般使用堆数据结构实现，堆一般使用数组存储
   集合中的每个元素都有一个权重值，每次出队都弹出优先级最大或最小的元素。
   • PriorityQueue是一个小顶堆；
   • PriorityQueue是非线程安全的；
   • PriorityQueue不是有序的，只有堆顶存储着最小的元素；
   • 入队就是堆的插入元素的实现；
   • 出队就是堆的删除元素的实现
   
### ConcurrentLinkedQueue与LinkedBlockingQueue对比
   
   （1）两者都是线程安全的队列；
   （2）两者都可以实现取元素时队列为空直接返回null，后者的poll()方法可以实现此功能；
   （3）前者全程无锁，后者全部都是使用重入锁控制的；
   （4）前者效率较高，后者效率较低；
   （5）前者无法实现如果队列为空等待元素到来的操作；
   （6）前者是非阻塞队列，后者是阻塞队列；
   （7）前者无法用在线程池中，后者可以；
