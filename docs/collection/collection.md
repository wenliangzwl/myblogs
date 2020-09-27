## java 集合
    
  ![集合类继承关系](collection.assets/collection.png)

### List
   Java中常用的数据类型。List是有序的collection。
    
   一共有三个实现类:
        
        1.ArrayList:最常用的list实现类,内部通过数组实现,数组的缺点是每个元素之间不能有分隔，当数组的大小不能满足需要，
        需要增加存储能力，就要将已经有数组的数据复制到新的存储空间。
        优缺点:适合随机查询，不利于删除，插入。
        2.Vector:与ArrayList一样，也是通过数组来实现,不同的是它支持多线程的同步，即某一个时刻只有一个线程能够写vector，
        避免多线程同时写引起的数据不一致，当实现同步也是要很高的消费的，因此访问比ArrayList要慢。
        3.LinkedList:使用链表结构存储数据
        优缺点: 适合动态的插入,删除，查询慢
        
### Map
    
   实现类
       
       1.HashMap ：线程不安全的，基于哈希表实现。Key值和value值都可以为null 。根据键的hashcode值来存储数据，
        底层实现使用数组+链表+红黑树。
       2.HashTable ：线程安全的，基于哈希表实现。Key值和value值不能为null。是一个遗留类，不建议使用。
       通过synchronized把这个table表锁住来实现线程安全，效率十分低下。
       3.TreeMap ：线程不安全的，保存元素key-value不能为null，允许key-value重复；遍历元素时随机排列。
       红黑树实现。在需要对一个有序的key集合进行遍历的时候建议使用。
       4.synchronizedMap：它其实就是加了一个对象锁，每次操作hashmap都需要先获取这个对象锁，
       这个对象锁有加了synchronized修饰，锁性能跟hashtable差不多。
       5.ConcurrentHashMap:是线程安全的。
       6.LinkHashMap：是HashMap的一个子类，增加了一条双向链表，从而可以保存记录的插入顺序。

### Set
   Set注重独一无二的性质。无序，不重复。
   
   实现类:
        
       HashSet：基于hash表实现的，存入数据按照hash值，所以无序，不是按照我们存储的顺序来排序。
        为了保证存入的一致性，存入元素哈希值相同的时候，会使用equals方法来比较，如果不同就放在同一个哈希桶中。
       TreeSet：基于红黑树实现，支持有序操作，每增加一个对象都会进行排序，讲对象插入二叉树指定的位置。
       LinkHashSet(HashSet+LinkedHashMap):继承于HashSet,又是基于LinkedHashMap来实现的，具有hashSet的查询效率。


###  源码分析

####  ArrayList

  特性:实现了三个标记接口：RandomAccess, Cloneable, java.io.Serializable

```
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
```   
   ArrayList 是基于数组实现的，所以支持快速随机访问。 继承了AbstractList，实现了List。它是一个数组队列，提供了相关的添加、删除、修改、遍历等功能。
   RandomAccess 接口标识着该类支持快速随机访问（只是一个定义了类型的接口，无作用）。  
   Cloneable 接口，即覆盖了函数 clone()，能被克隆(支持拷贝)。 
   java.io.Serializable 接口，这意味着ArrayList支持序列化，能通过序列化去传输。
   数组的默认大小为 10。


   浅拷贝
        
        基础类型的变量拷贝之后是独立的，不会随着源变量变动而变
        String类型拷贝之后也是独立的
        引用类型拷贝的是引用地址，拷贝前后的变量引用同一个堆中的对象
```
public Object clone() throws CloneNotSupportedException {
    Study s = (Study) super.clone();
    return s;
}
```
   深拷贝
        
        变量的所有引用类型变量（除了String）都需要实现Cloneable（数组可以直接调用clone方法），clone方法中，引用类型需要各自调用clone，重新赋值
        
```
public Object clone() throws CloneNotSupportedException {
    Study s = (Study) super.clone();
    s.setScore(this.score.clone());
    return s;
}
```
    ava的传参，基本类型和引用类型传参
    java在方法传递参数时，是将变量复制一份，然后传入方法体去执行。复制的是栈中的内容
    所以基本类型是复制的变量名和值，值变了不影响源变量
    引用类型复制的是变量名和值（引用地址），对象变了，会影响源变量（引用地址是一样的）
    String：是不可变对象，重新赋值时，会在常量表新生成字符串（如果已有，直接取他的引用地址），将新字符串的引用地址赋值给栈中的新变量，因此源变量不会受影响
    
