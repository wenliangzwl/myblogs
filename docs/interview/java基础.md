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
      
      new 一个对象，然后对象.getClass() 方法
      
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

#### 
   
   
