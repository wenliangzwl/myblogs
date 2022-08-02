### Spring 面试题
   
#### 什么是spring?

   Spring 是一个java 企业级应用的开源开发框架。Spring 主要是用来开发java 应用，但是有些扩展是针对构建J2EE平台的web应用。Spring框架目标是简化
   Java企业级应用开发，并通过POJO为基础的编程模型促进良好的编程习惯。

#### Spring 框架的设计目标，设计理念，和核心是什么？
   
   *Spring设计目标*: 为开发者提供一个一站轻量级应用开发平台
   
   *Spring设计理念*:  在java EE 开发中, 支持POJO 和javaBean 开发方式，使应用面向接口开发，充分支持OO (面向对象)设计方法;
    Spring 通过IOC 容器实现对象耦合关系的管理,并实现依赖反转，将对象之间的依赖关系交给Ioc 容器，实现解耦。
    
   *Spring框架的核心*: Ioc 容器和AOP 模块。通过IOC 容器管理POJO 对象以及他们之间的耦合关系；通过AOP 以动态非入侵的方式增强服务。
   IOC 让相互协作的组件保持松散的耦合，而AOP 编程运行你把遍布于应用各层的功能分离出来形成可重用的功能组件.

#### 谈谈对 IOC 的理解 

   通常，我们认为spring 有两大特性 IOC 和  AOP 

   对于很多人来说，ioc 这个概念给人的感觉就是 好像会，但是说不出来。 

   那么ioc 到底是什么，接下来说说我的理解，实际上这是一个非常大的问题，所以需要把它拆开来回答，io 表示控制反转,那么: 

      1. 什么是控制? 控制了什么? 
      2. 什么是反转，反转之前是谁控制的，反转之后又是谁控制的？如何控制的。
      3. 为什么要反转，反转之前有什么问题？ 反转之后有什么好处？ 

   这就是解决这一类大问题的思路，大而化小。 

   那么，先解决第一个问题，什么是控制，控制了什么？ 

   在用spring 的时候，我们需要做什么: 
      
      1. 建一些类，比如UserService、OrderService
      2. 用一些注解，比如 @Autowired 

   但是，我们也知道，当程序运行时，用的是具体的Userservice对象，OrderService 对象，那这些对象是什么时候创建的，谁创建的，包括对象里的属性是什么时候赋的值？ 
   谁赋的？ 所有这些都是我们程序员做的，以为我们只是写了类而已，所有的这些都是spring 做的，它才是幕后黑手。

   这就是控制: 
   
      1. 控制对象的创建
      2. 控制对象内属性的赋值。

   如果我们不用spring ，那么我们就得自己来做这两件事，反过来，用spring ,这两件事情就不用我们来做了，我们要做的仅仅是定义类，以及定义哪些属性需要spring 来赋值
   (比如某个属性上加 @Autowired) ,而这其实就是第二个问题的答案，这就是反转， 表示一种对象控制权的转移。 

   那反转有什么用，为什么要反转？

   如果我们自己来负责创建对象，自己来给对象中的属性赋值，会出现什么情况？ 
   
   比如现在有三个类： 

      1. A类， A类中有一个属性C c; 
      2. B类，B类中有一个属性 C c;
      3. C类; 

   现在 程序要运行，这三个类的对象都需要创建出来，并且相应的属性都需要有值，那么除开定义的这三个类之外，我们还得写： 

      1. A a = new A();
      2. B b = new B(); 
      3. C c = new C();
      4. a.c = c; 
      5. b.c = c; 

   这五行代码是不用spring 情况下多出来的代码，而且，如果类再多一些，类中的属性再多一些，那相应的代码会更多，而且代码会更复杂。所以我们可以发现，我们自己来控制
   比交给spring 来控制，我们的代码量以及代码复杂度是要高很多的，反言之，将对象交给spring 来控制，减轻了程序员的负担。 

   总结，ioc 表示控制反转，表示如果用spring ,那么spring 会负责来创建对象，以及给对象内的属性赋值，也就是如果用spring ,那么对象的控制权会转交给spring。

#### 单例Bean 和单例模式 

   单例模式 表示 jvm 中 某个类 只会存在唯一一个 

   而单例bean 并不表示 jvm 中 只能存在唯一的某个类的 Bean 对象。

#### 什么是Spring IOC 容器 ？
   
   SpringIOC 负责创建对象,管理对象(通过依赖注入(DI)),装配对象,配置对象,并且管理这些对象的整个生命周期。

#### 什么是Spring 的依赖注入?
   
   控制反转IOC 是一个很大的概念，可以用不同的方式来实现，主要实现方式有两种：依赖注入和依赖查找
   
   依赖注入: 相对于IoC 来说，依赖注入(DI)更加准确地描述了IoC 的设计理念。所谓依赖注入，即组件之间的依赖关系由容器在应用系统运行期来决定，
   也是由容器动态的将某种依赖关系的目标对象实例注入到应用系统的各个组件之中。组件不做定位查询，只提供普通的java方法让容器去决定依赖关系。
   
##### 有哪些不同类型的依赖注入实现方式?

   Setter 方法注入和构造器注入
   
   构造器注入：构造器依赖注入通过容器触发一个类的构造器来实现，该类有一系列参数，每个参数代表一个对其他类的依赖。
   
   Setter方法注入: Setter 方法注入是容器通过调用无参构造器或无参static 工厂方法实例化bean 之后，调用该bean 的setter 方法,
   即实现了基于setter 的依赖注入。
   
   两种依赖依赖方式都可以使用，最好的解决方案是用构造器参数实现强制依赖，setter 方法实现可选依赖。

#### 什么是bean 装配?
   
   装配，或bean装配是指在Spring 容器中把bean 组装到一起,前提是容器需要知道bean 的依赖关系，如何通过依赖注入来把它们装配到一起。 
    
#### 什么是bean 的自动装配？
   
   在spring 框架中，在配置文件中设定bean的依赖关系是一个很好的机制，
   spring 容器能够自动装配相互合作的bean,这意味着容器不需要配置，能通过Bean工厂自动处理bean 之间的协作。这意味着Spring 可以通过向
   BeanFactory 中注入的方式自动搞定bean之间的依赖关系。自动装配可以设置在每个bean上，也可以设定在特定的bean 上。
    
#### 解释不同方式的自动装配，spring自动装配bean 有哪些方式？
   
   在spring 中，对象无需自己查找或创建与其关联的其他对象，由容器负责把需要相互协作的对象引用赋予各个对象，使用autowire 来配置自动装载模式。
   
   在spring 框架xml 配置中共有五种自动装配:
    
     no： 默认的方式是不进行自动装配的，通过手工设置ref 属性来进行装配bean.
     
     byName: 通过bean 的名称进行自动装配，如果一个bean 的property 与另一bean的name相同，就进行自动装配。
     
     byType： 通过参数的数据类型进行自动装配
     
     constructor: 利用构造函数进行装配,并且构造函数的参数通过byType 进行装配。
     
     autodetect： 自动探测，如果有构造方法，通过construct 的方式自动装配，否则使用byType 的方式自动装配

#### 使用@Autowired 注解自动装配的过程是怎样的？
   
   在启动spring ioc 时，容器自动装载了一个AutowiredAnnotationBeanPostProcessor 后置处理器，当容器扫描到@Autowired 、
   @Resource 或 @Inject 时，就会在IOc 容器中自动查找需要的bean ,并装配给该对象的属性。在使用@Autowired 时，首先在容器中查询对应
   类型的bean: 
       
       1.如果查询结果刚好为一个，就将该bean 装配给@Autowired 指定的数据
       
       2.如果查询的结果不止一个，那么@Autowired 会根据名称来查找
       
       3.如果上述查找结果为空，那么会抛出异常。解决方法时，使用required = false。 
   
#### Spring 框架中的单例bean 是线程安全的吗？
    
   不,Spring 框架中的单例bean 不是线程安全的。 
   
   spring 中的bean 默认是单例模式,spring 框架并没有对单例bean 进行多线程的封装处理。
   
   实际上大部分时候spring bean 是无状态的（比如dao 类）,所以某种程度上来说bean 也是安全的，但如果bean 有状态的话(比如view model 对象)
   ，那就得需要开发者自己去保证线程安全，最简单的就是改变bean 的作用域，把singleton 变为 prototype,这样请求 bean 相当于new Bean()了。所以
   就可以保证线程安全了。
      
      有状态就是有数据存储功能
      无状态就是不会保存数据

### Spring 如何处理线程并发问题？
   
   在一般情况下，只有无状态的Bean 才可以在多线程环境下共享,在spring 中 ，绝大部分Bean 都可以声明未singleton 作用域，因为spring 对一些Bean 
   中非线程安全状态采用ThreadLocal 进行处理,解决线程安全问题.
   
   ThreadLocal 和线程同步机制都是为了解决多线程相同变量的访问冲突问题。同步机制采用了“时间换空间”的方式, 仅提供一份变量,不同线程在访问钱需要获取锁
   ，没获取到锁的线程需排队。而ThreadLocal 采用了 “空间换时间” 的方式。
   
   ThreadLocal 会为每一个线程提供一个独立的副本,从而隔离了多个线程对数据的访问冲突。因为每一个线程都拥有自己的变量副本，从而也就没有必要对改变量进行同步le
   ThreadLocal 提供了线程安全的共享对象，在编写多线程代码时,可以把不安全的变量封装进ThreadLocal。
   
#### Spring  框架中都用到了哪些设计模式? 
   
   1.工厂模式: BeanFactory 就是简单工厂模式的体现，用来创建对象的实例;
   
   2.单例模式: Bean 默认为单例模式
   
   3.代理模式: SpringAOP功能用到了JDK的动态代理个CGLIB字节码生成技术；
   
   4.模板方法: 用来解决代码重复的问题.比如RestTemplate 
   
   5.观察者模式: 定义对象键一种一对多的依赖关系，当一个对象的状态发送改变时，所以依赖于它的对象都会得到通知被制动更新。如Spring 中listener 
   的实现-ApplicationListener 

   6. 原型模式: 原型bean 

   7. 构造器模式: BeanDefinitionBuilder 

   8. 适配器模式: AdvisorAdapter、ApplicationListenerMethodAdapter 

   9. 装饰器模式: BeanWrapper 

   10. 策略模式: BeanNameGenerator、 InstatiationStrategy 

   11. 责任链模式: DefaultAdvisorChainFactory 

#### Spring IOC 的实现机制
   
   Spring IOC 的实现原理就是工厂模式加反射机制

#### BeanFactory 和 AppliactionContext 有什么区别？
   
   BeanFactory 和 ApplicationContext 是spring 的两大核心接口, 都可以当做Spring 的容器,。其中ApplicationContext 是BeanFactory 
   的子接口。
   
   BeanFactory: 是spring 里面最底层的接口,包含了各种Bean 的定义,读取bean 配置文档,管理bean 的加载、实例化、控制bean 的生命周期，维护bean
   之间的依赖关系
   
   ApplicationContext: 作为BeanFactory 的派生,除了提供BeanFactory s所具备的功能外,还提供了更完整的框架功能。（比如: 获取系统环境变量、国际化、事件发布等）
   
#### 什么是Spring 的内部bean ? 什么是Spring inner beans ？
   
   在Spring 框架中,当一个bean 仅被用作另一个bean 的属性时,它能被声明为一个内部bean。内部bean 可以用setter 注入 “属性”和构造方法注入“构造参数”
   的方式来实现,内部bean 通常是匿名的，他们的scope 一般是prototype。 
   
#### @Autowired 和 @Resource 之间的区别
   
   @Autowired 可用于：构造函数、成员变量、Setter 方法
   @Autowired 和@Resource 之间的区别
       
       @Autowired 默认是按照类型装配注入的，默认情况下它要求依赖对象必须存在(可以设置它required 属性未false)
       
       @Reource 默认是按照名称来装配注入的，只有当找不到与名称匹配的bean 才会按照类型装配注入.

#### Spring AOP and AspectJ AOP 有什么区别? AOP 有哪些实现方式?
   
   AOP 实现的关键在于代理模式，AOP 代理主要分为静态代理和动态代理。静态代理的代表为AspectJ；动态代理则以Spring AOP 为代表。
   
   (1) AspectJ 是静态代理的增强，所谓静态代理，就是AOP 框架会在编译阶段生成AOP 代理类, 因此也称为编译时增强，他会在编译阶段将AspectJ(切面)
   织入到java 字节码中，运行的时候就是增强之后的AOP 对象.
   
   (2) Spring AOP 使用的是动态代理，所谓动态代理就是说AOP 框架不会去修改字节码，而是每次运行时在内存中临时为方法生成AOP 对象，这个AOP 对象包含了
   目标对象的全部方法，并且在特定的切点做了增强处理，并回调原对象的方法。
   
#### FactoryBean 和BeanFactory 的区别？

   BeanFactory 是接口,提供了IOC容器最基本的形式,给具体的IOC容器的实现提供了规范
   
   FactoryBean 也是接口，为IOC 容器中的Bean 的实现提供了更加灵活的方式，FactoryBean 在IOC 容器的基础上
   给Bean 的实现加上了一个简单工厂模式和装饰模式，可以在getObject() 方法中灵活配置。
   
   区别: BeanFactory 是Factory,也就是IOC 容器或对象工厂，FactoryBean 是个Bean。在Spring中,
   所有的Bean 都是由BeanFactory(也就是IOC容器)来进行管理的。
   
   但对FactoryBean 而言，这个Bean不是简单的Bean,而是一个能生产或者修饰对象生成的工厂Bean,它的实现
   与设计模式中的工厂模式和装饰器模式类似
   
#### 在 Spring 事务中执行多条 SQL 语句时，是对应多个数据库连接还是一个数据库连接？

   （回答：一个连接，多个就无法保证事务了）

#### 既然是一个连接，Spring 在前后执行多条 SQL 时，是如何保证当前线程获取到同一个连接的？

   (回答：当时没想到 ThreadLocal，面试官引导说，如果是你，会怎么实现。后来回答说存在一个 map，key 是当前线程，value 就是数据库连接，最后灵光一现，想到了 ThreadLocal)
    
#### Spring 的 AOP 的使用场景？AOP 机制有什么好处？

#### 事务的隔离级别和传播行为

   事务的隔离级别

      read uncommited：是最低的事务隔离级别，它允许另外一个事务可以看到这个事务未提交的数据。
      read commited：保证一个事物提交后才能被另外一个事务读取。另外一个事务不能读取该事物未提交的数据。
      repeatable read：这种事务隔离级别可以防止脏读，不可重复读。但是可能会出现幻象读。它除了保证一个事务不能被另外一个事务读取未提交的数据之外还避免了以下情况产生（不可重复读）。
      serializable：这是花费最高代价但最可靠的事务隔离级别。事务被处理为顺序执行。除了防止脏读，不可重复读之外，还避免了幻象读（避免三种）。
   
   propagion_xxx:事务的传播行为

      保证同一个事务中
      PROPAGATION_REQUIRED 支持当前事务，如果不存在 就新建一个(默认) -- required
      PROPAGATION_SUPPORTS 支持当前事务，如果不存在，就不使用事务    -- supports
      PROPAGATION_MANDATORY 支持当前事务，如果不存在，抛出异常      --  mandatory
      
      保证没有在同一个事务中
      PROPAGATION_REQUIRES_NEW 如果有事务存在，挂起当前事务，创建一个新的事务  -- requires_new
      PROPAGATION_NOT_SUPPORTED 以非事务方式运行，如果有事务存在，挂起当前事务 -- not_supported
      PROPAGATION_NEVER 以非事务方式运行，如果有事务存在，抛出异常             -- never
      PROPAGATION_NESTED 如果当前事务存在，则嵌套事务执行                     -- nested


#### spring 事务什么时候会失效？ 

   spring 事务的原理是 AOP , 进行了切面增强，那么失效的根本原因就是这个 aop 不起作用，常见的情况有以下几种: 

      1. 发生自调用，类里面使用this 调用本类的方法(this通常省略)， 此时这个this 对象不是代理对象，而是 UserService 对象本身
      解决方法: 让那个this 变成 UserService 的代理类即可。

      2. 方法 不是 public 的 @Transactional 只能用于 public 的方法上，否则事务不会生效，如果要用在 非 public 方法上，可以开启 AspectJ 代理模式 

      3. 数据库不支持事务

      4. 没有被spring 管理 

      5. 异常被吃掉，事务不会回滚(或者抛出的异常没有被定义，默认为 RuntimeException)

#### spring 中的事务是如何实现的？ 

   1. spring 事务底层是基于数据库事务和AOP机制实现的。
   
   2. 首先对 使用了 @Transactional 注解的bean ，spring 会创建一个代理对象 

   3. 当 调用代理对象的方法时，会先判断该方法上是否加了 @Transactional 注解。

   4. 如果加了，那么则利用事务管理器 创建一个数据库连接。 

   5. 并且修改 数据库连接的 autocommit 属性为 false, 禁止此连接的自动提交，这是实现 spring 事务非常重要的一步。

   6. 然后执行 当前方法，方法中会执行 sql 

   7. 执行完当前方法 后，如果没有出现异常就直接提交事务.

   8. 如果出现了异常，并且这个异常是需要回滚的就会回滚事务，否则 仍然提交事务。

   9. spring 事务隔离级别对应的就是 底层数据库的事务隔离级别

   10. spring 事务的传播机制是spring 事务自己实现的，也是spring 事务中最复杂的一块。

   11. spring 事务的传播机制是基于 数据库连接来做的，一个数据库连接一个事务，如果传播机制配置为新开一个事务，那么实际上就是先建立一个数据库连接， 再次数据库连接上执行sql.

#### Spring Bean 创建的生命周期有哪些步骤

   1. 推断构造方法
   
   2. 实例化 ,Instantiation

   3. 填充属性 ,Populate

   4. 处理 Aware 回调 

   5. 初始化前 ， 处理 @PostConstruct 注解 

   6. 初始化，处理 Initialization 接口

   7. 初始化后 进行 AOP 

   8. 销毁 Destruction

#### spring 容器启动流程是怎么样的

   1. 在创建spring 容器，也就是启动 spring 时，

   2. 首先会进行扫描，扫描得到所有的BeanDefinition 对象，并存入 Map 中。 

   3. 然后 筛选出非懒加载的单例 BeanDefinition 进行创建 Bean, 对于多例 Bean 不需要在启动中进行创建，对于多例Bean 会在每次获取Bean 时 利用 BeanDefinition 
   去创建。 
      
   4. 利用 BeanDefinition 创建bean 就是 bean 的生命周期，这期间包括了 合并 BeanDefinition ，推断构造方法、实例化、属性填充、初始化前、初始化、初始化后等多个步骤，
      其中 AOP 就是发送在 初始化后。 

   5. 单例bean 创建完后，spring 会发布一个容器启动事件。 

   6. spring 启动结束。 

   7. 在源码中会更复杂，比如源码中会提供一些模板方法，让子类实现，比如源码中还会涉及一些 BeanFactoryPostProcessor 和 BeanPostProcessor 的注册，spring 的扫描就是 通过
      BeanFactoryPostProcessor来实现的，依赖注入就是通过 BeanPostProcessor 来实现的。 
      
   8. 在 spring 启动过程中 还会去处理 @Import 等注解。




