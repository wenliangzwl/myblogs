### java 基础篇 

#### 1. java 字符型常量和字符串常量的区别?
   
   形式上: 字符常量是单引号引起的一个字符；字符串常量是双引号引起的0个或若干个字符
   
   含义上: 字符常量相当于一个整型值(ASCII),可以参加表达式运算；字符串常量代表一个地址值(该字符串在内存中存放位置)
   
   占用内存大小 字符常量只占用2个字节； 字符串常量站占干个字节

#### 2. String 和StringBuilder 、StringBuffer 的区别?
   
   java 提供了三种类型的字符串: String、StringBuffer、StringBuilder ，它们可以存储和操作字符串.
   
   其中String 是只读 字符串，也就意味着String 引用的字符串内容是不能被改变的。
   
   StringBuffer/StringBuilder 类表示的字符串对象可以直接修改。StringBuilder 是java 5 中引入的，它和StringBuffer 的方法完全相同，
   区别在于它是单线程环境下使用的，因为它的所有方面都没有被synchronized 修饰,因此它的效率也比StringBuffer 要高。
#### 3. 反射的用途及实现
   
   java 反射机制是一个非常强大的功能，在很多框架中比如Spring mybatis 都可以看到反射。通过反射机制，我们可以在运行期间获取对象的信息。
   利用这个我们可以实现工厂模式和代理模式等设计模式。
   
   获取一个对象的反射类，有以下3种方式
      
      new 一个对象，然后对象.getClass()方法
      
      通过 Class.forName() 方法
      
      使用 类.class

#### 4. String s= "abc" 和String s = new String("abc") 区别
    
   new String("abc") 在内存中创建了几个对象 答案是：一个或者两个
    
   1. 首先在堆中(不是常量池)创建一个指定的对象 "abc", 并让s 引用指向该对象
    
   2. 在字符串常量池中查看，是否存在内容为"abc" 字符串对象
    
   3.若存在，则将new 出来的字符串对象与字符串常量池中的对象联系起来
    
   4.若不存在,则在字符串常量池中创建一个内容为 "abc" 的字符串对象,并将堆中的对象与之关联起来
    
####  5. java 中的SPI 


#### 6. java 8 有哪些特性
   
   1. 函数式接口
   
   2. 接口可以有实现方法，而且不需要实现类去实现其方法
   
   3. lambda 表达式
   
   4. strram 流 
   
   5. 日期时间API LocalDateTime 年月日时分秒 LocalDate 日期, LocalTime 时间
   
   6. Optional 类
#### 谈谈对⾯向对象（OOP）思想的理解？
   
   将复杂的事物抽象成为一个个个体，每个个体皆为对象，每个对象去处理自己的方法。
   
   优点: 易扩展、维护、复用。
   
   缺点: 性能低，因为封装，初始化需要实例化，比较耗资源.
   
   OOP 三大特征: "封装、继承、多态"
   
   理解：面向对象是把一组数据结构和处理他们的方法组成对象；把具有相同行为的对象归纳成类；
   通过封装隐藏类的内部细节；通过继承试类得到泛化；通过多态实现基于对象类型的动态分派；

   封装: 封装的意义，在于明确标识出允许外部使用的所有成员函数和数据项，内细节对外部透明，外部调用无需修改或者关心内部实现。 

   继承: 继承基类的方法，并作出自己的改变和扩展，子类共性的方法或者属性直接使用父类的，而不需要自己再定义，只需扩展自己的个性化的。

   多态: 基于对象所属类的不同，外部对同一个方法的调用，实际执行的逻辑不同。
    
#### == & equals 的区别
   
   "==": 比较基本数据类型是比较数值； 引用类型比较的是内存(引用)地址
   "equals": 判断两个变量或实例所指向的内存空间的值是否相同(比较内容是否相等)， 但是具体得看各个类重写equals 方法之后的比较逻辑，比如string 类，虽然是引用类型，但是重写了 equals 方法，
    方法内部比较的是字符串的各个字符是否全部相等

#### final 
   
   1. "final 修饰类" : 不可被继承 ，（拓展）避免滥用继承带来的危害
       
      继承缺点： 打破了封装性，和父类耦合，父类更新后可能会导致错误。 "复合优先于继承"
      "创建一个新的类，在新的类中添加私有域，引用现有类的一个实例，这种设计叫做复合"
   
   2. "final 修饰方法": 不可重写
   
   3. "final 修饰成员变量" 变量值不能被更改
#### 接口和抽象类的区别

   1. 抽象类可以存在普通成员函数，而接口中只能存在 public abstaract 方法 (java8 之后允许接口有默认方法)

   2. 抽象类中的成员变量可以是各种类型的，而接口中的成员变量只能是 pulic static final 类型的 (常量)。

   3. 抽象类只能继承一个，接口可以实现多个。

   4. "抽象类" 是用来捕捉子类通用特性的，描述一种事物，不能被实例化，只能被用作子类的超类,抽象类是被继承的子类模板。对类本质的抽象， 是 is-a 的关系 
   
   5. "接口" 接口是抽象方法的集合，是对类行为进行抽象， 对类 行为的抽象， 是 like-a 的关系.
   
   区别:
      
      1. "抽象类" 可以有默认方法的实现， "接口" 完全是抽象的，不存在方法的实现 (java8 之后允许接口有默认方法)
      
      2. "抽象类" 子类使用extends 来继承抽象类，如果子类不是抽象类，它必须提供抽象类里声明的所有抽象方法的实现方法.
            "接口" implements 来实现接口，需要提供所有接口声明的抽象方法的实现方法。
            
      3. "抽象类" 可以有构造器
      
      4. "抽象类" 可以有main 方法并且可以运行
      
      5. "接口" 是可以实现多继承
      
      6. "接口" 速度较慢，因为它需要去寻找类中的实现方法.
      
      7. "抽象类" 添加新方法可以给默认实现，所以不需要声明新方法的实现。接口可以用 "default" 一样的

#### int 和Integer 的区别
   
   Integer 是int 的包装类，Integer 必须实例化才可以使用 "有了自动装箱拆箱机制之后，也可以直接使用"
   
   "IntegerCache" 缓存 -128~127 之间的数值，不会new 新的Integer 对象，而是直接引用内存中常量池的对象
    
    1. int 是一个基本类型， Integer 是一个包装类型 

    2. jvm 为了性能和开销的考虑，所以引入了int， 但它不是面向对象处理的，Integer 是完全面向对象处理的

    3. int 的默认值为0， integer 默认值为null 

    4. 比较 int 使用 == 比较， Integer 使用 equals 比较， 如果使用 == 比较 可能 会出现问题，

    5. Integer 引入了一个缓存， IntegerCache 会缓存 -128~127 直接的数值，不会直接 new 新的对象 

    6. 如果是一些局部变量 最好使用 int ，因为它是分配在栈上的， 如果 使用的是 pojo 对象 ，则 使用Integer。

#### 什么是向上转型？ 什么是向下转型？ 
   
   1.向上转型： 一定是安全的，"子类转化成父类"
   
   2.向下转型: 是不安全的 "前提条件是子类还是原来的子类，否则会报转换异常"
#### 方法重写 与重载的区别
   
   "重写" 发生在父类子类中，方法名相同、参数列表相同，返回值范围小于等于父类，抛出的异常范围小于等于父类，访问修饰符范围大于等于父类; 如果父类方法访问修饰符为private 则子类就不能重写该方法。
   
   "重载" 发生在类里，方法名相同，参数类型不同、个数不同、顺序不同，方法返回值和访问修饰符可以不同，发送在编译时

```
public int add(int a, int b);
public String add(int a, int b)
// 编译时会报错 ，不是重载
```

#### List 和 Set 区别
   
   "list" 有序、重复, 按照对象进入的顺序保存数据，允许多个null 元素对象，可以使用iterator 取出所有元素，在逐一遍历，还可以使用get(int index) 获取指定下标的元素
   
   "set" 无序、不重复， 最多允许有一个null 元素对象，取数据时只能用 iterator 接口取得所有元素，再 逐一遍历各个元素。
    
        "无序不是可排序，而是加入的顺序是随机的"

```java
public class Main {

    public static void main(String[] args) {

        ArrayList<String> list = new ArrayList<>();

        list.add("1");
        list.add("2");
        // 允许多个空值
        list.add(null);
        list.add(null);

        // 根据 下标访问
        System.out.println(list.get(0));

        // 使用迭代器 访问
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            System.out.println(next);
        }

        System.out.println("======================");

        Set<String> set = new HashSet<>();

        set.add("11");
        set.add("22");
        // 最多允许一个空值
        set.add(null);
        set.add(null);

        Iterator<String> iterator1 = set.iterator();
        while (iterator1.hasNext()) {
            String next = iterator1.next();
            System.out.println(next);
        }
    }
}
```

#### ArrayList 和 LinkedList 的区别

   1. "数据结构不同": ArrayList 是基于动态数组， LinkedList 是基于链表
   
   2. "查" ArrayList 支持随机访问(下标index访问)，链表不支持高效随机访问的
   
   3. "改" ArrayList 在列表末尾增加一个元素开销是固定的。LinkedList 修改数据开销是统一的。在ArrayList
   的中间插入或删除一个元素意味着这个列表的剩余的元素都会被移动；
   
   4. "空间" ArrayList 的空间浪费主要体现在list列表的结尾预留一定的容量空间，而LinkedList 的空间花费则体现在它的
   每一个元素都需要消耗相当的空间
   
   5. "总结": 当操作是在一列数据的后面添加数据而不是在前面或中间，并且需要随机地访问其中的元素时，使用ArrayList 会提供比较好的性能;
   当你的操作是在一列数据的前面或中间添加或删除数据，并且安装顺序访问其中的元素时，就应该使用LinkedList 了。
      
   6. ArrayList 和 LinkedList 都实现了 List 接口，但是 LinkedList 还额外实现了 Deque 接口，所以LinkedList 还可以当做队列来使用

#### CopyOnWriteArrayList 的底层原理是怎样的

   1. CopyOnWriteArrayList 内部也是通过数组来实现的，在向 CopyOnWriteArrayList 添加元素时，会复制一个新的数组，写操作在新数组上进行，读操作在原数组上进行

   2. 并且 写操作会加锁，防止出现并发写入丢失数据问题。

   3. 写操作结束之后会把原数组指向新数组

   4. CopyOnWriteArrayList 允许在写操作时来读取数据，大大提高了读的性能，因此适合读多写少 的应用场景，但是 CopyOnWriteArrayList 会比较占内存，同时可能 
      读到的数据不是实时最新的数据，所以不适合实时性要求很高的场景。

#### 实现线程的方式
   
   继承Thread 
       
      "优点": 可以用this直接获取当前线程
      
      "缺点": 因为java是不支持多继承的，所以不能再继承其他的父类了
   
   实现Runable接口
        
      "优点"： 可以继承其他对象，这样就可以多个线程共享同一个目标对象了
      
      "缺点": 代码复杂，如果需要访问当前线程，必须使用Thread.currentThread()方法.
        
   实现Callable 接口 (可以获取线程执行之后的返回值)
        
      "优点": Callable 的任务执行后可返回值
   
   Runable 和 Callable 的区别:
      
     (1)(Callable) 规定的方法是call(), Runable 规定的方法是run()
     
     (2) Callable 的任务执行后可返回值，而Runable 的任务是不能返回值的
     
     (3) call 方法可以抛出异常，run 方法不可以，因为run 方法本身没有抛出异常，所以自定义的线程类在重写run
     的时候也无法抛出异常
     
     (4) 运行Callable 任务可以拿到一个Future 对象，表示异步计算结果。它提供了检查计算是否完成的方法，以等待计算完成，并检索
     计算的结果。通过Future 对象可以了解任务执行情况，可取消任务的执行，还可获取执行结果。

#### 谈谈 Sleep 和wait 的区别
    
   1.sleep 不会释放锁，wait 会释放
   
   2.wait 只能在同步(synchronize) 环境中被调用，而sleep 不需要。
   
   3. 进入wait 状态的线程能够被notify 和notifyAll 线程唤醒，但是进入sleeping 状态的线程不能被notify 方法唤醒
   
   4. wait 通常有条件地执行，线程会一直处于wait 状态，直到某个条件变为真。但是sleep 仅仅让你的线程进入睡眠状态。
   
   5. wait 方法是针对一个被同步代码块加锁的对象，而sleep 是针对一个线程。 
#### Java的四种引⽤，强弱软虚，⽤到的场景
   
   1. 强引用
          
       强引用是使用最普遍的引用。如果一个对象具有强引用，那垃圾回收器绝不会回收它。如下：
       
          Object o=new Object();   //  强引用
       
       当内存空间不足，Java虚拟机宁愿抛出OutOfMemoryError错误，使程序异常终止，也不会靠随意回收具有强引用的对象来解决内存不足的问题。如果不使用时，要通过如下方式来弱化引用，如下：
      
          o=null;     // 帮助垃圾收集器回收此对象
       
       显式地设置o为null，或超出对象的生命周期范围，则gc认为该对象不存在引用，这时就可以回收这个对象。具体什么时候收集这要取决于gc的算法。
       
       
       
   2. 软引用
       
       如果一个对象只具有软引用，则内存空间足够，垃圾回收器就不会回收它；如果内存空间不足了，就会回收这些对象的内存。只要垃圾回收器没有回收它，
       该对象就可以被程序使用。软引用可用来实现内存敏感的高速缓存。
       
       String str=new String("abc");         // 强引用
       
       SoftReference<String> softRef=new SoftReference<String>(str);     // 软引用
       
       当内存不足时，等价于：
       
       If(JVM.内存不足()) {
       
         str = null;  // 转换为软引用
       
         System.gc(); // 垃圾回收器进行回收
       }
       
       软引用在实际中有重要的应用，例如浏览器的后退按钮。按后退时，这个后退时显示的网页内容是重新进行请求还是从缓存中取出呢？这就要看具体的实现策略了。
       
       （1）如果一个网页在浏览结束时就进行内容的回收，则按后退查看前面浏览过的页面时，需要重新构建
       
       （2）如果将浏览过的网页存储到内存中会造成内存的大量浪费，甚至会造成内存溢出
       
       这时候就可以使用软引用
       
       Browser prev = new Browser();               // 获取页面进行浏览
       
       SoftReference sr = new SoftReference(prev); // 浏览完毕后置为软引用    
       
       if(sr.get()!=null){ 
       
       rev = (Browser) sr.get();           // 还没有被回收器回收，直接获取
       
       }else{
       
       prev = new Browser();               // 由于内存吃紧，所以对软引用的对象回收了
       
       sr = new SoftReference(prev);       // 重新构建
       
       }
       
       这样就很好的解决了实际的问题。
       
       软引用可以和一个引用队列（ReferenceQueue）联合使用，如果软引用所引用的对象被垃圾回收器回收，Java虚拟机就会把这个软引用加入到与之关联的引用队列中。
       
   3、弱引用（WeakReference）
       
        弱引用与软引用的区别在于：只具有弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。不过，由于垃圾回收器是一个优先级很低的线程，因此不一定会很快发现那些只具有弱引用的对象。
        
        String str=new String("abc");    
        
        WeakReference<String> abcWeakRef = new WeakReference<String>(str);
        
        str=null;
        
        当垃圾回收器进行扫描回收时等价于：
        
        str = null;
        
        System.gc();
        
   4、虚引用
        
        “虚引用”顾名思义，就是形同虚设，与其他几种引用都不同，虚引用并不会决定对象的生命周期。如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收。
        
        虚引用主要用来跟踪对象被垃圾回收器回收的活动。虚引用与软引用和弱引用的一个区别在于：虚引用必须和引用队列（ReferenceQueue）联合使用。当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在回收对象的内存之前，
        把这个虚引用加入到与之关联的引用队列中。
    
   5、总结
       
       Java4种引用的级别由高到低依次为：
       
       强引用 > 软引用 > 弱引用 > 虚引用
       
       通过图来看一下他们之间在垃圾回收时的区别：
       
       当垃圾回收器回收时，某些对象会被回收，某些不会被回收。垃圾回收器会从根对象Object来标记存活的对象，然后将某些不可达的对象和一些引用的对象进行回收.
#### volatile 关键字的作⽤和原理
   
   作用: 
       
       1. volatile 保证可见性
       
       2. 禁止进行指令重排序
       
       3. volatile 不能确保原子性
       
   原理: 
       
       “观察加入volatile关键字和没有加入volatile关键字时所生成的汇编代码发现，加入volatile关键字时，会多出一个lock前缀指令”
       
       lock前缀指令实际上相当于一个内存屏障（也成内存栅栏），内存屏障会提供3个功能：
        
            1. 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；
       
            2. 它会强制将对缓存的修改操作立即写入主存；
       
            3. 如果是写操作，它会导致其他CPU中对应的缓存行无效。

#### final、finally、finalize 的区别？
   
   final: 用于声明属性，方法和类，分别表示属性不可变，方法不可覆盖，被其修饰的类不可继承。
   
   finally: 异常处理语句结构的一部分，表示总是执行。
   
   finalize: Object 类的一个方法，所有Java对象都有这个方法，当某Java对象没有更多的引用指向的时候，会被垃圾回收器回收，
   该对象被回收之前，由垃圾回收器来负责调用此方法，通常在该方法中进行回收前的准备工作。该方法更像一个对象生命周期的临终方法，
   当该方法被系统调用则代表对象即将"死亡"，但是需要注意的是，我们主动行为上去调用该方法并不会导致该对象"死亡"，这是一个
   被动的方法(其实就是回调方法)，不需要我们调用。

#### Error 类和 Exception 类的父类都是Throwable 类，他们的区别如下:
   
   Error 类一般是指与虚拟机相关的问题，如系统崩溃，虚拟机错误，内存空间不足，方法调用栈溢出等。
   对于这类错误的导致应用程序中断，仅靠程序本身无法恢复和预防，遇到这样的错误,建议让程序终止。
   
   Exception 类表示程序可以处理的异常，可以捕获且可能恢复。遇到这类异常，应该尽可能处理异常，使程序恢复运行，而
   不应该随意终止异常。
   
   Exception 类 又分为未检查异常(UnCheckedException) 和受检查的异常(CheckedException)。
   
   运⾏时异常ArithmeticException，IllegalArgumentException编译能通过，但是⼀运⾏就终⽌了，程序不会处理运⾏时异常，出
   现这类异常，程序会终⽌。
   
   ⽽受检查的异常，要么⽤ try…catch 捕获，要么⽤throws字句声明抛出，交给它的⽗类处理，否则编译不会通过。

#### 接⼝幂等性
   
   什么是接口幂等性:
   
    同一个接口，多次发出同一个请求，必须保证操作只执行一次。
   
   为什么会产生接口幂等性问题：
    
       1. 网络波动，可能会引发重复请求
       
       2. 用户重复操作，用户在操作时候可能会无意触发多次下单交易，甚至没有响应而有意触发多次交易应用。
       
       3. 使用了失效或超时重试机制（Nginx 重试， RPC 重试或业务层重试）

       4. 页面重复刷新
       
       5. 使用浏览器后退按钮重复之前的操作，导致重复表单提交
       
       6. 使用浏览器历史记录重复提交表单
       
       7. 浏览器重复的HTTP 请求
       
       8. 定时任务重复执行
       
       9. 用户双击提交按钮
           
   解决方案:
       
       1. 唯一索引 使用唯一索引可以避免脏数据的添加，当插入重复数据时数据库会抛异常，保证了数据的唯一性。
       
       2. 乐观锁 这里的乐观锁指的是用乐观锁的原理去实现，为数据字段增加一个version字段，当数据需要更新时，先去数据库里获取此时的version版本号
       
       3. 悲观锁 乐观锁可以实现的往往用悲观锁也能实现，在获取数据时进行加锁，当同时有多个重复请求时其他请求都无法进行操作
       
       4. 分布式锁 幂等的本质是分布式锁的问题，分布式锁正常可以通过redis或zookeeper实现；在分布式环境下，锁定全局唯一资源，使请求串行化，实际表现为互斥锁，防止重复，解决幂等。
       
       5. token机制 token机制的核心思想是为每一次操作生成一个唯一性的凭证，也就是token。一个token在操作的每一个阶段只有一次执行权，一旦执行成功则保存执行结果。对重复的请求，返回同一个结果。token机制的应用十分广泛。


#### JDK 、 JRE、 JVM 之间的区别 

   JDK(java SE Development kit): java 标准开发包，它提供了编译、允许java 程序所需的各种工具和资源，包括java 编译器、java运行时环境、以及常用的java类库等 

   JRE(Java Runtime Environment): java 运行环境，用于运行java 的字节码文件，JRE 中包括JVM 以及 JVM 工作所需要的类库，普通用户而只需要安装JRE来运行java 程序，而程序开发者必须安装jdk 来编译、调试程序。 

   JVM(java Virtual Mechinal): java 虚拟机，是jre 中的一部分，它是整个java 实现跨平台的最核心的部分，复制运行字节码文件。

   jvm 在执行java字节码时，需要把字节码解释为机器指令，而不同的操作系统的机器指令是有可能不一样的，所以就导致不同的操作系统上的jvm 是不一样的。所以在安装jdk 时需要选择操作系统. 

#### hashCode 与 equals() 之间的关系 

   在java 中，每个对象都可以调用自己的hashCode() 方法得到自己的哈希值(hashCode), 相对于对象的指纹信息，通常来说世界上没有完全相同的两个指纹，但是在java 中 做不到这么绝对，但是我们仍然可以利用hashCode 
   来做一些提前的判断，比如: 

    1. 如果两个对象的hashCode 不相等，那么这两个对象肯定不是同一个对象

    2. 如果两个对象的hashCode 相等，不代表这两个对象一定是同一个对象，也可能是两个对象

    3. 如果两个对象相等，那么他们的 hashCode 就一定相同。

   在java的一些集合类的实现中，在比较两个对象是否相等时，会根据上面的原则，会先调用对象的hashCode() 方法得到hashCode 进行比较，如果hashCode 不相同，就可以直接认为这两个对象不相同，如果
   hashCode 相同，那么就会进一步调用equals() 方法进行比较，而 equals() 方法，就是用来最终确认两个对象是不是相等的，通常equals 方法的实现会比较重，逻辑比较多，而 hashCode() 主要就是得到一个哈希值， 
   实际上就一个数字，相对而言比较轻，所以比较两个对象时，通常都会先根据 hashCode 先比较一下. 

   注意: 重写了 equals() 方法，那么就要注意 重新 hashCode() 方法，一定要保证能遵守上述规则。

#### 泛型中 extends 和 super 的区别 

   1. <? extends T> 表示包括T在内的任何T的子类

   2. <? super T> 表示包括T 在内的任何T的 父类 

#### loadClass() 和 forName() 的区别 

   两者都可以返回 Class 对象，区别就是 类加载的程度不同。

   loadClass() 方法返回的Class 对象默认只进行了加载 阶段，而连接 和初始化 阶段还没有进行的，所以说，使用 loadClass() 方法默认是不会触发 该类里面的静态
    代码块的。

   forName() 方法返回的Class 对象默认已经进行完初始化了，所以说会触发 该类里面的静态代码块。

   比如说像 jdbc 中的 Driver 类是通过forName() 方法来获取的，就是因为 Driver 类里面有一个静态代码块，所以需要使用 forName()

   而 loadClass() 在 spring ioc 中延迟加载用的很广泛。

#### 双亲委派机制及其作用 

    双亲委派机制是类加载器的工作模型，它使得类加载器有了存在优先级高低的层级结构。

    双亲委派机制的核心思想是： 当一个类加载器收到类加载的请求时，它不会马上自己加载，而是将加载请求委托给父类，在父类中也会重复这么一个操作。只有当父类
    无法完成加载时，自己才会尝试加载。

    双亲委派的两个好处: 

    1. 避免了 类的重复加载

    2. 沙箱安全。

#### Java 中使用的是值传递还是引用传递？

   当一个对象被当作参数传递到一个方法后，此方法可改变这个对象的属性，并可返回变化后的结果，那么这里到底是值传递还是引用传递?

   是值传递。Java 编程语言只有值传递参数。当一个对象实例作为一个参数被传递到方法中时，参数的值就是对该对象的引用。对象的内容可以在被调用的方法中改变，但对象的引用是永远不会改变的。

#### 深拷贝和浅拷贝

   深拷贝和浅拷贝就是指对象的拷贝，一个对象存在两种类型的拷贝，一种是基本数据类型，一种是实例对象的引用。
   
   1. 浅拷贝是指，只会拷贝基本数据类型的值，以及实例对象的内存地址，并不会复制一份引用地址所指的对象，也就是浅拷贝出来的对象，内部的类属性指向的是同一个对象。

   2. 深拷贝是指，即会拷贝基本数据类型的值，也会针对实例对象的引用地址所指的对象进行拷贝，深拷贝出来的对象，内部属性指向的不是同一个对象。