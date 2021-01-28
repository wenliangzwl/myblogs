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

#### Spring IOC 的实现机制
   
   Spring IOC 的实现原理就是工厂模式加反射机制

#### BeanFactory 和 AppliactionContext 有什么区别？
   
   BeanFactory 和 ApplicationContext 是spring 的两大核心接口, 都可以当做Spring 的容器,。其中ApplicationContext 是BeanFactory 
   的子接口。
   
   BeanFactory: 是spring 里面最底层的接口,包含了各种Bean 的定义,读取bean 配置文档,管理bean 的加载、实例化、控制bean 的生命周期，维护bean
   之间的依赖关系
   
   ApplicationContext: 作为BeanFactory 的派生,除了提供BeanFactory s所具备的功能外,还提供了更完整的框架功能。
   
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
   
   