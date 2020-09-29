##  源码分析

###  ArrayList

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
    java的传参，基本类型和引用类型传参
    java在方法传递参数时，是将变量复制一份，然后传入方法体去执行。复制的是栈中的内容
    所以基本类型是复制的变量名和值，值变了不影响源变量
    引用类型复制的是变量名和值（引用地址），对象变了，会影响源变量（引用地址是一样的）
    String：是不可变对象，重新赋值时，会在常量表新生成字符串（如果已有，直接取他的引用地址），将新字符串的引用地址赋值给栈中的新变量，因此源变量不会受影响
    
#### 基本属性
```
private static final long serialVersionUID = 8683452581122892189L;//序列化版本号（类文件签名），如果不写会默认生成，类内容的改变会影响签名变化，导致反序列化失败
private static final int DEFAULT_CAPACITY = 10;//如果实例化时未指定容量，则在初次添加元素时会进行扩容使用此容量作为数组长度
//static修饰，所有的未指定容量的实例(也未添加元素)共享此数组，两个空的数组有什么区别呢？ 就是第一次添加元素时知道该 elementData 从空的构造函数还是有参构造函数被初始化的。以便确认如何扩容。空的构造器则初始化为10，有参构造器则按照扩容因子扩容
private static final Object[] EMPTY_ELEMENTDATA = {};
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
transient Object[] elementData; // arrayList真正存放元素的地方，长度大于等于size
private int size;//arrayList中的元素个数
```
#### 构造器
```
//无参构造器，构造一个容量大小为 10 的空的 list 集合，但构造函数只是给 elementData 赋值了一个空的数组，其实是在第一次添加元素时容量扩大至 10 的。
public ArrayList() {
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}
//当使用无参构造函数时是把 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 赋值给 elementData。 当 initialCapacity 为零时则是把 EMPTY_ELEMENTDATA 赋值给 elementData。 当 initialCapacity 大于零时初始化一个大小为 initialCapacity 的 object 数组并赋值给 elementData。
public ArrayList(int initialCapacity) {
    if (initialCapacity > 0) {
        this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
        this.elementData = EMPTY_ELEMENTDATA;
    } else {
        throw new IllegalArgumentException("Illegal Capacity: "+ initialCapacity);
    }
}
//将 Collection 转化为数组，数组长度赋值给 size。 如果 size 不为零，则判断 elementData 的 class 类型是否为 ArrayList，不是的话则做一次转换。 如果 size 为零，则把 EMPTY_ELEMENTDATA 赋值给 elementData，相当于new ArrayList(0)。
public ArrayList(Collection<? extends E> c) {
    Object[] a = c.toArray();
    if ((size = a.length) != 0) {
        if (c.getClass() == ArrayList.class) {
            elementData = a;
        } else {
            elementData = Arrays.copyOf(a, size, Object[].class);
        }
    } else {
        // 指向空数组
        elementData = EMPTY_ELEMENTDATA;
    }
}
```
#### 添加元素--默认尾部添加
```
//每次添加元素到集合中时都会先确认下集合容量大小。然后将 size 自增 1赋值
public boolean add(E e) {
    ensureCapacityInternal(size + 1);  
    elementData[size++] = e;
    return true;
}
//判断如果 elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA 就取 DEFAULT_CAPACITY 和 minCapacity 的最大值也就是 10。这就是 EMPTY_ELEMENTDATA 与 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 的区别所在。同时也验证了上面的说法：使用无参构造函数时是在第一次添加元素时初始化容量为 10 的
private void ensureCapacityInternal(int minCapacity) {
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    ensureExplicitCapacity(minCapacity);
}
//对modCount自增1，记录操作次数，如果 minCapacity 大于 elementData 的长度，则对集合进行扩容,第一次添加元素时 elementData 的长度为零
private void ensureExplicitCapacity(int minCapacity) {
    modCount++;
    if (minCapacity - elementData.length > 0)
        grow(minCapacity);
}
涉及扩容，会消耗性能，但是如果提前指定容量，会提升性能，可以达到与linkedList相当，甚至超越
public void addEffect(){
    //不指定下标插入
    int length = 10000000;
    List al = new ArrayList(length);//指定容量时   效率相当
    List ll = new LinkedList();
    long start5 = System.currentTimeMillis();
    for(int i=0;i <length;i++){
        al.add(i);
    }
    long end5 = System.currentTimeMillis();
    System.out.println(end5-start5);
    long start6 = System.currentTimeMillis();
    for(int i=0;i <length;i++){
        ll.add(i);
    }
    long end6 = System.currentTimeMillis();
    System.out.println(end6-start6);
}
执行结果：
912
4237
```
#### 指定下标添加元素
```
public void add(int index, E element) {
    rangeCheckForAdd(index);//下标越界检查
    ensureCapacityInternal(size + 1);  //同上  判断扩容,记录操作数
    //依次复制插入位置及后面的数组元素，到后面一格，不是移动，因此复制完后，添加的下标位置和下一个位置指向对同一个对象
    System.arraycopy(elementData, index, elementData, index + 1,
                     size - index);
    elementData[index] = element;//再将元素赋值给该下标
    size++;
}
```
   时间复杂度为O(n)，与移动的元素个数正相关

#### 扩容
```
private void grow(int minCapacity) {
    int oldCapacity = elementData.length;//获取当前数组长度
    int newCapacity = oldCapacity + (oldCapacity >> 1);//默认将扩容至原来容量的 1.5 倍
    if (newCapacity - minCapacity < 0)//如果1.5倍太小的话，则将我们所需的容量大小赋值给newCapacity
        newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)//如果1.5倍太大或者我们需要的容量太大，那就直接拿 newCapacity = (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE 来扩容
        newCapacity = hugeCapacity(minCapacity);
    elementData = Arrays.copyOf(elementData, newCapacity);//然后将原数组中的数据复制到大小为 newCapacity 的新数组中，并将新数组赋值给 elementData。
}
```
#### 删除元素
```
public E remove(int index) {
    rangeCheck(index);//首先会检查 index 是否合法
    modCount++;//操作数+1
    E oldValue = elementData(index);
    int numMoved = size - index - 1;
    if (numMoved > 0)//判断要删除的元素是否是最后一个位,如果 index 不是最后一个，就从 index + 1 开始往后所有的元素都向前拷贝一份。然后将数组的最后一个位置空,如果 index 是最后一个元素那么就直接将数组的最后一个位置空
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    elementData[--size] = null; //让指针最后指向空，进行垃圾回收
    return oldValue;
}
//当我们调用 remove(Object o) 时，会把 o 分为是否为空来分别处理。然后对数组做遍历，找到第一个与 o 对应的下标 index，然后调用 fastRemove 方法，删除下标为 index 的元素。
public boolean remove(Object o) {
    if (o == null) {
        for (int index = 0; index < size; index++)
            if (elementData[index] == null) {
                fastRemove(index);
                return true;
            }
    } else {
        for (int index = 0; index < size; index++)
            if (o.equals(elementData[index])) {
                fastRemove(index);
                return true;
            }
    }
    return false;
}
//fastRemove(int index) 方法和 remove(int index) 方法基本全部相同。
private void fastRemove(int index) {
    modCount++;
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,numMoved);
    elementData[--size] = null; 
}
```
#### 迭代器 iterator
```
public Iterator<E> iterator() {
    return new Itr();
}
private class Itr implements Iterator<E> {
    int cursor;       // 代表下一个要访问的元素下标
    int lastRet = -1; // 代表上一个要访问的元素下标
    int expectedModCount = modCount;//代表对 ArrayList 修改次数的期望值，初始值为 modCount
    //如果下一个元素的下标等于集合的大小 ，就证明到最后了
    public boolean hasNext() {
        return cursor != size;
    }
    @SuppressWarnings("unchecked")
    public E next() {
        checkForComodification();//判断expectedModCount和modCount是否相等,ConcurrentModificationException
        int i = cursor;
        if (i >= size)//对 cursor 进行判断，看是否超过集合大小和数组长度
            throw new NoSuchElementException();
        Object[] elementData = ArrayList.this.elementData;
        if (i >= elementData.length)
            throw new ConcurrentModificationException();
        cursor = i + 1;//自增 1。开始时，cursor = 0，lastRet = -1；每调用一次next方法，cursor和lastRet都会自增1。
        return (E) elementData[lastRet = i];//将cursor赋值给lastRet，并返回下标为 lastRet 的元素
    }
    public void remove() {
        if (lastRet < 0)//判断 lastRet 的值是否小于 0
            throw new IllegalStateException();
        checkForComodification();//判断expectedModCount和modCount是否相等,ConcurrentModificationException
        try {
            ArrayList.this.remove(lastRet);//直接调用 ArrayList 的 remove 方法删除下标为 lastRet 的元素
            cursor = lastRet;//将 lastRet 赋值给 curso
            lastRet = -1;//将 lastRet 重新赋值为 -1，并将 modCount 重新赋值给 expectedModCount。
            expectedModCount = modCount;
        } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
        }
    }
    final void checkForComodification() {
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }
}
```

  remove 方法的弊端。
  1、只能进行remove操作，add、clear 等 Itr 中没有。
  2、调用 remove 之前必须先调用 next。因为 remove 开始就对 lastRet 做了校验。而 lastRet 初始化时为 -1。
  3、next 之后只可以调用一次 remove。因为 remove 会将 lastRet 重新初始化为 -1

#### 不可变集合
```
Collections.unmodifiableList可以将list封装成不可变集合（只读），但实际上会受源list的改变影响
public void unmodifiable() {
    List list = new ArrayList(Arrays.asList(4,3,3,4,5,6));//缓存不可变配置
    List modilist = Collections.unmodifiableList(list);//只读
    modilist.set(0,1);//会报错UnsupportedOperationException
    //modilist.add(5,1);
    list.set(0,1);
    System.out.println(modilist.get(0));//打印1
}
```

#### Arrays.asList
```
public void testArrays(){
    long[] arr = new long[]{1,4,3,3};
    List list = Arrays.asList(arr);//基本类型不支持泛型化，会把整个数组当成一个元素放入新的数组，传入可变参数
    System.out.println(list.size());//打印1
}
//可变参数
public static <T> List<T> asList(T... a) {
        return new ArrayList<>(a);
}
```

#### 什么是fail-fast？
    
    fail-fast机制是java集合中的一种错误机制。
    当使用迭代器迭代时，如果发现集合有修改，则快速失败做出响应，抛出ConcurrentModificationException异常。
    这种修改有可能是其它线程的修改，也有可能是当前线程自己的修改导致的，比如迭代的过程中直接调用remove()删除元素等。
    另外，并不是java中所有的集合都有fail-fast的机制。比如，像最终一致性的ConcurrentHashMap、CopyOnWriterArrayList等都是没有fast-fail的。
    fail-fast是怎么实现的：
    ArrayList、HashMap中都有一个属性叫modCount，每次对集合的修改这个值都会加1，在遍历前记录这个值到expectedModCount中，遍历中检查两者是否一致，如果出现不一致就说明有修改，则抛出ConcurrentModificationException异常。
    底层数组存/取元素效率非常的高(get/set)，时间复杂度是O(1)，而查找（比如：indexOf，contain），插入和删除元素效率不太高，时间复杂度为O(n)。
    插入/删除元素会触发底层数组频繁拷贝，效率不高，还会造成内存空间的浪费，解决方案：linkedList
    查找元素效率不高，解决方案：HashMap（红黑树）

###  LinkedList
  特性
```
public class LinkedList<E> extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
```
   
   1、继承于 AbstractSequentialList ，本质上面与继承 AbstractList 没有什么区别，AbstractSequentialList 完善了 AbstractList 中没有实现的方法。
   2、Serializable：成员变量 Node 使用 transient 修饰，通过重写read/writeObject 方法实现序列化。
   3、Cloneable：重写clone（）方法，通过创建新的LinkedList 对象，遍历拷贝数据进行对象拷贝。
   4、Deque：实现了Collection 大家庭中的队列接口，说明他拥有作为双端队列的功能。
   LinkedList与ArrayList最大的区别就是LinkedList中实现了Collection中的 Queue（Deque）接口 拥有作为双端队列的功能

#### 基本属性
   链表没有长度限制，他的内存地址不需要分配固定长度进行存储，只需要记录下一个节点的存储地址即可完成整个链表的连续。
```
//当前有多少个结点，元素个数
transient int size = 0;
//第一个结点
transient Node<E> first;
//最后一个结点
transient Node<E> last;
//Node的数据结构
private static class Node<E> {
    E item;//存储元素
    Node<E> next;//后继
    Node<E> prev;//前驱
    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
```
  
   LinkedList 在1.6 版本以及之前，只通过一个 header 头指针保存队列头和尾。这种操作可以说很有深度，但是从代码阅读性来说，却加深了阅读代码的难度。因此在后续的JDK 更新中，将头节点和尾节点 区分开了。
   节点类也更名为 Node。
   为什么Node这个类是静态的？答案是：这跟内存泄露有关，Node类是在LinkedList类中的，也就是一个内部类，若不使用static修饰，那么Node就是一个普通的内部类，在java中，一个普通内部类在实例化之后，
   默认会持有外部类的引用，这就有可能造成内存泄露（内部类与外部类生命周期不一致时）。但使用static修饰过的内部类（称为静态内部类），就不会有这种问题
   
   非静态内部类会自动生成一个构造器依赖于外部类：也是内部类可以访问外部类的实例变量的原因
   静态内部类不会生成，访问不了外部类的实例变量，只能访问类变量

#### 构造器
```
public LinkedList() {
}
public LinkedList(Collection<? extends E> c) {
    this();
    addAll(c);//操作次数只会记录一次   设置前驱后继
}
```

#### 添加元素
```
public boolean add(E e) {
     linkLast(e);
     return true;
 }
//目标节点创建后寻找前驱节点， 前驱节点存在就修改前驱节点的后继，指向目标节点
void linkLast(E e) {
    final Node<E> l = last;//获取这个list对象内部的Node类型成员last，即末位节点，以该节点作为新插入元素的前驱节点
    final Node<E> newNode = new Node<>(l, e, null);//创建新节点
    last = newNode;//把新节点作为该list对象的最后一个节点
    if (l == null)//处理原先的末位节点，如果这个list本来就是一个空的链表
        first = newNode;//把新节点作为首节点
    else
        l.next = newNode;//如果链表内部已经有元素，把原来的末位节点的后继指向新节点，完成链表修改
    size++;//修改当前list的size，
    modCount++;//并记录该list对象被执行修改的次数
}
public void add(int index, E element) {
    checkPositionIndex(index);//检查下标的合法性
    if (index == size)//插入位置是末位，那还是上面末位添加的逻辑
        linkLast(element);
    else
        linkBefore(element, node(index));
}
private void checkPositionIndex(int index) {
    if (!isPositionIndex(index))
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}
private boolean isPositionIndex(int index) {
    return index >= 0 && index <= size;
}
Node<E> node(int index) {
    if (index < (size >> 1)) {//二分查找   index离哪端更近 就从哪端开始找
        Node<E> x = first;
        for (int i = 0; i < index; i++)
            x = x.next;//找到index位置的元素
        return x;
    } else {
        Node<E> x = last;
        for (int i = size - 1; i > index; i--)
            x = x.prev;
        return x;
    }
}
//指位添加方法核心逻辑  操作新节点，紧接修改原有节点的前驱属性，最后再修改前驱节点的后继属性
void linkBefore(E e, Node<E> succ) {
    final Node<E> pred = succ.prev;//原位置节点的前驱pred
    final Node<E> newNode = new Node<>(pred, e, succ);//创建新节点,设置新节点其前驱为原位置节点的前驱pred，其后继为原位置节点succ
    succ.prev = newNode;//将新节点设置到原位置节点的前驱
    if (pred == null)//前驱如果为空，空链表，则新节点设置为first
        first = newNode;
    else
        pred.next = newNode;//将新节点设置到前驱节点的后继
    size++;//修改当前list的size
    modCount++;//记录该list对象被执行修改的次数。
}
public boolean addAll(int index, Collection<? extends E> c) {
    checkPositionIndex(index);
    //将集合转化为数组
    Object[] a = c.toArray();
    int numNew = a.length;
    if (numNew == 0)
        return false;
    Node<E> pred, succ;
    //获取插入节点的前节点（prev）和尾节点（next）
    if (index == size) {
        succ = null;
        pred = last;
    } else {
        succ = node(index);
        pred = succ.prev;
    }
    //将集合中的数据编织成链表
    for (Object o : a) {
        @SuppressWarnings("unchecked") E e = (E) o;
        Node<E> newNode = new Node<>(pred, e, null);
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        pred = newNode;
    }
    //将 Collection 的链表插入 LinkedList 中。
    if (succ == null) {
        last = pred;
    } else {
        pred.next = succ;
        succ.prev = pred;
    }
    size += numNew;
    modCount++;
    return true;
}
```
  
  final修饰，不希望在运行时对变量做重新赋值
  LinkedList 在插入数据优于ArrayList ，主要是因为他只需要修改指针的指向即可，而不需要将整个数组的数据进行转移。
  而LinkedList 由于没有实现 RandomAccess，或者说不支持索引搜索的原因，他在查找元素这一操作，需要消耗比较多的时间进行操作（n/2）。

#### 删除元素
  1、AbstractSequentialList的remove
```
public E remove(int index) {
    checkElementIndex(index);
    //node(index)找到index位置的元素
    return unlink(node(index));
}
//remove(Object o)这个删除元素的方法的形参o是数据本身，而不是LinkedList集合中的元素（节点），所以需要先通过节点遍历的方式，找到o数据对应的元素，然后再调用unlink(Node x)方法将其删除
public boolean remove(Object o) {
    if (o == null) {
        for (Node<E> x = first; x != null; x = x.next) {
            if (x.item == null) {
                unlink(x);
                return true;
            }
        }
    } else {
        for (Node<E> x = first; x != null; x = x.next) {
            if (o.equals(x.item)) {
                unlink(x);
                return true;
            }
        }
    }
    return false;
}
E unlink(Node<E> x) {
    //x的数据域element
    final E element = x.item;
    //x的下一个结点
    final Node<E> next = x.next;
    //x的上一个结点
    final Node<E> prev = x.prev;
    //如果x的上一个结点是空结点的话，那么说明x是头结点
    if (prev == null) {
        first = next;
    } else {
        prev.next = next;//将x的前后节点相连   双向链表
        x.prev = null;//x的属性置空
    }
    //如果x的下一个结点是空结点的话，那么说明x是尾结点
    if (next == null) {
        last = prev;
    } else {
        next.prev = prev;//将x的前后节点相连   双向链表
        x.next = null;
    }
    x.item = null;//指向null  方便GC回收
    size--;
    modCount++;
    return element;
}
```
 
  2、Deque 中的Remove
```
//将first 节点的next 设置为新的头节点，然后将 f 清空。 removeLast 操作也类似。
private E unlinkFirst(Node<E> f) {
    final E element = f.item;
    //获取到头结点的下一个结点           
    final Node<E> next = f.next;
    f.item = null;
    f.next = null; // 方便 GC
    //头指针指向的是头结点的下一个结点
    first = next;
    //如果next为空，说明这个链表只有一个结点
    if (next == null)
        last = null;
    else
        next.prev = null;
    size--;
    modCount++;
    return element;
}
```

#### 双端链表（队列Queue）

    java中队列的实现就是LinkedList： 我们之所以说LinkedList 为双端链表，是因为他实现了Deque 接口；我们知道，队列是先进先出的，添加元素只能从队尾添加，删除元素只能从队头删除，Queue中的方法就体现了这种特性。 支持队列的一些操作，我们来看一下有哪些方法实现：
    • pop（）是栈结构的实现类的方法，返回的是栈顶元素，并且将栈顶元素删除
    • poll（）是队列的数据结构，获取对头元素并且删除队头元素
    • push（）是栈结构的实现类的方法，把元素压入到栈中
    • peek（）获取队头元素 ，但是不删除队列的头元素
    • offer（）添加队尾元素
    可以看到Deque 中提供的方法主要有上述的几个方法，接下来我们来看看在LinkedList 中是如何实现这些方法的。
  
  1.1、队列的增
  offer（）添加队尾元素
```
public boolean offer(E e) {
    return add(e);
}
具体的实现就是在尾部添加一个元素
```
  
  1.2、队列的删
  poll（）是队列的数据结构，获取对头元素并且删除队头元素
```
public E poll() {
    final Node<E> f = first;
    return (f == null) ? null : unlinkFirst(f);
}
具体的实现，删除的是队列头部的元素
```
   
  1.3、队列的查
```
public E peek() {
    final Node<E> f = first;
    return (f == null) ? null : f.item;
}
```

  1.4、栈的增
  push（）是栈结构的实现类的方法，把元素压入到栈中
  push（） 方法的底层实现，其实就是调用了 addFirst（Object o）
```
public void push(E e) {
    addFirst(e);
}
```
 
  1.5、栈的删
  pop（）是栈结构的实现类的方法，返回的是栈顶元素，并且将栈顶元素删除
```
public E pop() {
    return removeFirst();
}
public E removeFirst() {
    final Node f = first;
    if (f == null)
    throw new NoSuchElementException();
    return unlinkFirst(f);
}
```

### CopyOnWriteArrayList

#### 特性
```
public class CopyOnWriteArrayList<E>
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
```

#### 基本属性
```
private static final long serialVersionUID = 8673264195747942595L;//序列化版本号
//全局锁
final transient ReentrantLock lock = new ReentrantLock();
//存储数据的数组
private transient volatile Object[] array;
```

#### 构造器
```
public CopyOnWriteArrayList() {
    setArray(new Object[0]);//创建一个大小为0的Object数组作为array初始值
}
public CopyOnWriteArrayList(E[] toCopyIn) {
    //创建一个list，其内部元素是toCopyIn的的副本
    setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
}
//将传入参数集合中的元素复制到本list中
public CopyOnWriteArrayList(Collection<? extends E> c) {
    Object[] elements;
    if (c.getClass() == CopyOnWriteArrayList.class)
        elements = ((CopyOnWriteArrayList<?>)c).getArray();
    else {
        elements = c.toArray();
        // c.toArray可能不是Object[]（比如：继承ArrayList，重写toArray方法返回String[]，
        //只有ArrayList的toArray方法实现是Arrays.copyOf，因此在jdk8中，此处改为了ArrayList.class）
        if (elements.getClass() != Object[].class)
            elements = Arrays.copyOf(elements, elements.length, Object[].class);
    }
    setArray(elements);
}
```

#### 添加元素
```
public boolean add(E e) {
    final ReentrantLock lock = this.lock;
    lock.lock();//先加锁
    try {
        Object[] elements = getArray();
        int len = elements.length;
        Object[] newElements = Arrays.copyOf(elements, len + 1);//复制到新数组中，长度+1
        newElements[len] = e;//在新数组中添加元素
        setArray(newElements);//将新数组设置给array
        return true;
    } finally {
        lock.unlock();
    }
}
```

#### 指定位置添加元素
```
public void add(int index, E element) {
    final ReentrantLock lock = this.lock;
    // 加锁
    lock.lock();
    try {
        // 获取旧数组
        Object[] elements = getArray();
        int len = elements.length;
        // 检查是否越界, 可以等于len
        if (index > len || index < 0)
            throw new IndexOutOfBoundsException("Index: "+index+
                                                ", Size: "+len);
        Object[] newElements;
        int numMoved = len - index;
        if (numMoved == 0)
            // 如果插入的位置是最后一位
            // 那么拷贝一个n+1的数组, 其前n个元素与旧数组一致
            newElements = Arrays.copyOf(elements, len + 1);
        else {
            // 如果插入的位置不是最后一位
            // 那么新建一个n+1的数组
            newElements = new Object[len + 1];
            // 拷贝旧数组前index的元素到新数组中
            System.arraycopy(elements, 0, newElements, 0, index);
            // 将index及其之后的元素往后挪一位拷贝到新数组中
            // 这样正好index位置是空出来的
            System.arraycopy(elements, index, newElements, index + 1,
                             numMoved);
        }
        // 将元素放置在index处
        newElements[index] = element;
        setArray(newElements);
    } finally {
        // 释放锁
        lock.unlock();
    }
}
```

#### addIfAbsent
```
//添加一个不存在于集合中的元素。
public boolean addIfAbsent(E e) {
    // 获取元素数组
    Object[] snapshot = getArray();
    //已存在返回false，否则添加
    return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false :
        addIfAbsent(e, snapshot);
}

private boolean addIfAbsent(E e, Object[] snapshot) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        // 重新获取旧数组
        Object[] current = getArray();
        int len = current.length;
        // 如果快照与刚获取的数组不一致，说明有修改
        if (snapshot != current) {
            // 重新检查元素是否在刚获取的数组里，减少indexOf的对比次数
            int common = Math.min(snapshot.length, len);
            for (int i = 0; i < common; i++)
                //判断是否有线程指定下标添加了元素
                if (current[i] != snapshot[i] && eq(e, current[i]))
                    return false;
            if (indexOf(e, current, common, len) >= 0)
                    return false;
        }
        // 拷贝一份n+1的数组
        Object[] newElements = Arrays.copyOf(current, len + 1);
        // 将元素放在最后一位
        newElements[len] = e;
        setArray(newElements);
        return true;
    } finally {
        // 释放锁
        lock.unlock();
    }
}
```

#### 获取指定位置元素
```
public E get(int index) {
    return get(getArray(), index);
}
final Object[] getArray() {
    return array;
}
//私有方法
private E get(Object[] a, int index) {
    return (E) a[index];
}
```

  这个方法是线程不安全的，因为这个分成了两步，分别是获取数组和获取元素，而且中间过程没有加锁。
  假设当前线程在获取数组（执行getArray()）后，其他线程修改了这个CopyOnWriteArrayList，那么它里面的元素就会改变，
  但此时当前线程返回的仍然是旧的数组，所以返回的元素就不是最新的了，这就是写时复制策略产生的弱一致性问题。
  
#### 修改指定位置元素
```
public E set(int index, E element) {
    final ReentrantLock lock = this.lock;
    lock.lock();    
    try {
        Object[] elements = getArray();
        E oldValue = get(elements, index);//先获取要修改的旧值
        if (oldValue != element) {//值确实需要修改
            int len = elements.length;
            //将array复制到新数组
            Object[] newElements = Arrays.copyOf(elements, len);            
            newElements[index] = element;//修改元素
            setArray(newElements);//设置array为新数组
        } else {
            // 虽然值不需要改，但要保证volatile语义，需重新设置array
            setArray(elements);
        }
        return oldValue;
    } finally {
        lock.unlock();
    }
}
```
#### 删除元素
```
public E remove(int index) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        Object[] elements = getArray();
        int len = elements.length;
        E oldValue = get(elements, index);//获取要删除的元素
        int numMoved = len - index - 1;
        if (numMoved == 0)//删除的是最后一个元素
            setArray(Arrays.copyOf(elements, len - 1));
        else {
            //将元素分两次复制到新数组中
            Object[] newElements = new Object[len - 1];
            System.arraycopy(elements, 0, newElements, 0, index);//拷贝index前面的元素
            System.arraycopy(elements, index + 1, newElements, index,numMoved);//拷贝index后面的元素
            setArray(newElements);
        }
        return oldValue;
    } finally {
        lock.unlock();
    }
}
```
#### 弱一致性的迭代器
   指返回迭代器后，其他线程对list的增删改对迭代器是不可见的
```
public Iterator<E> iterator() {
    return new COWIterator<E>(getArray(), 0);   //返回一个COWIterator对象
}
static final class COWIterator<E> implements ListIterator<E> {
    //数组array快照
    private final Object[] snapshot;
    //遍历时的数组下标
    private int cursor;
    private COWIterator(Object[] elements, int initialCursor) {
        cursor = initialCursor;
        snapshot = elements;//保存了当前list的内容
    }
    public boolean hasNext() {
        return cursor < snapshot.length;
    }
    @SuppressWarnings("unchecked")
    public E next() {
        if (! hasNext())
            throw new NoSuchElementException();
        return (E) snapshot[cursor++];
    }
```
   如果在返回迭代器后没有对里面的数组array进行修改，则这两个变量指向的确实是同一个数组；但是若修改了，则根据前面所讲，它是会新建一个数组，然后将修改后的数组复制到新建的数组，而老的数组就会被“丢弃”，
   所以如果修改了数组，则此时snapshot指向的还是原来的数组，而array变量已经指向了新的修改后的数组了。这也就说明获取迭代器后，使用迭代器元素时，其他线程对该list的增删改不可见，因为他们操作的是两个不同的数组，
   这就是弱一致性。
   CopyOnWriteArrayList使用写时复制策略保证list的一致性，而获取–修改–写入三个步骤不是原子性，所以需要一个独占锁保证修改数据时只有一个线程能够进行。另外，CopyOnWriteArrayList提供了弱一致性的迭代器，
   从而保证在获取迭代器后，其他线程对list的修改是不可见的，迭代器遍历的数组是一个快照。

#### 使用场景及优点
   并发容器用于读多写少的并发场景。比如白名单，黑名单等场景。
   读操作可能会远远多于写操作的场景。比如，有些系统级别的信息，往往只需要加载或者修改很少的次数，但是会被系统内所有模块频繁的访问。对于这种场景，我们最希望看到的就是读操作可以尽可能的快，而写即使慢一些也没关系。
   CopyOnWriteArrayList 的思想比读写锁的思想更进一步。为了将读取的性能发挥到极致，CopyOnWriteArrayList 读取是完全不用加锁的，更厉害的是，写入也不会阻塞读取操作，也就是说你可以在写入的同时进行读取，只有写入和写入之间需要进行同步，也就是不允许多个写入同时发生，但是在写入发生时允许读取同时发生。这样一来，读操作的性能就会大幅度提升。
   读写分离

#### 缺点
  内存占用，弱一致性

### ArrayList 和 LinkedList 区别

#### 新增元素
   
  两者的起始长度一样：
    
    如果是从集合的头部新增元素，ArrayList 花费的时间应该比 LinkedList 多，因为需要对头部以后的元素进行复制。
    如果是从集合的中间位置新增元素，ArrayList 花费的时间搞不好要比 LinkedList 少，因为 LinkedList 需要遍历。
    如果是从集合的尾部新增元素，ArrayList 花费的时间应该比 LinkedList 少，因为数组是一段连续的内存空间，也不需要复制数组；
    而链表需要创建新的对象，前后引用也要重新排列。

#### 删除元素
    
    从集合头部删除元素时，ArrayList 花费的时间比 LinkedList 多很多；    
    从集合中间位置删除元素时，ArrayList 花费的时间比 LinkedList 少很多；
    从集合尾部删除元素时，ArrayList 花费的时间比 LinkedList 少一点。

####  遍历元素
    ArrayList:
        get(int)：根据索引找元素,非常快
        indexOf(Object)：根据元素找索引，需要遍历整个数组了，从头到尾依次找，慢。
    LinkedList：
        get(int)：找指定位置上的元素，需要调用 node(int) 方法，就意味着需要前后半段遍历。
        indexOf(Object)：找元素所在的位置,需要遍历整个链表，和 ArrayList 的 indexOf() 类似。
    集合遍历两种做法:(for,迭代器)
         for: LinkedList 在 get 的时候性能会非常差，因为每一次外层的 for 循环，都要执行一次 node(int) 方法进行前后半段的遍历。
         迭代器:迭代器只会调用一次 node(int) 方法，在执行 list.iterator() 的时候：先调用 AbstractSequentialList 类的 iterator() 方法，再调用 AbstractList 类的 listIterator() 方法，再调用 LinkedList 类的 listIterator(int) 方法
    
    遍历 LinkedList 的时候，千万不要使用 for 循环，要使用迭代器。
    也就是说，for 循环遍历的时候，ArrayList 花费的时间远小于 LinkedList；迭代器遍历的时候，两者性能差不多。
        
   



