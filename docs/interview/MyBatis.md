### MyBatis 面试题
   
#### 什么是Mybatis ？
    
   1. mybatis 是一个半orm (对象关系映射) 框架 ,内部封装了JDBC ,开发时只要关注sql 语句本身,不需要花费精力去处理加载驱动，创建连接，创建statement 
   等繁杂的过程,可以让开发者直接编写原生sql,严格控制sql 执行性能，灵活度高。
   
   2. Mybatis 可以使用XML 和注解的形式来配置和映射原生信息，将POJO 映射成数据库中的记录，避免了手动设置参数以及获取结果集。
   
   3. 通过xml 文件或注解的方式将要执行的各种statement 配置起来, 并通过java对象和statement 中sql 的动态参数进行映射生成最终执行的sql 语句。
   最后由mybatis 框架执行sql 并将结果映射为java 对象并返回。
   
#### Mybatis 的优点 
   
   1. 基于sql 语句编程,相当灵活，sql 写在XML 里,解除sql 与程序代码的耦合，便于统一管理,提供XML 标签，支持编写动态SQL 语句，并可重用。
   
   2. 与JDBC 相比，减少50%以上的代码量， 消除了JDBC 大量冗余的代码，不需要手动开关连接。
   
   3. 与各种数据库兼容 （因为Mybatis 使用JDBC 来连接数据库，所以只要JDBC 支持的数据库mybatis 都支持）
   
   4. 与spring 有很好的集成
   
   5. 提供映射标签，支持对象与数据库的ORM 字段关系映射；提供对象关系映射标签，支持对象关系组件维护。

#### Mybatis 的缺点 
   
   1. SQL 语句的编写工作量较大，尤其当字段多，关联表多时，对开发人员编写SQL 语句的功底有一定要求。
   
   2. sql 语句依赖于数据库，导致数据库移植性差，不能随意更换数据库。
   
#### MyBatis 与Hibernate 有哪些不同？
   
   1、Mybatis 和 hibernate 不同，它不完全是一个 ORM 框架，因为 MyBatis 需要程序员自己编写 Sql 语句。
   
   2、Mybatis 直接编写原生态 sql， 可以严格控制 sql 执行性能， 灵活度高， 非常适合对*关系数据模型*要求不高的软件开发， 因为这类软件需求变化频繁， 
   一但需求变化要求迅速输出成果。但是灵活的前提是 mybatis 无法做到数据库无关性， 如果需要实现支持多种数据库的软件，则需要自定义多套 sql 映射文件，工作量大。
   
   3、Hibernate 对象/关系映射能力强， 数据库无关性好， 对于*关系模型*要求高的软件， 如果用 hibernate 开发可以节省很多代码， 提高效率。
   
#### #{}和${}的区别是什么？

   *#{}是预编译处理， ${}是字符串替换。*
   
   Mybatis 在处理#{}时，会将 sql 中的#{}替换为?号，调用 PreparedStatement 的set 方法来赋值；
   
   Mybatis 在处理${}时， 就是把${}替换成变量的值。使用#{}可以有效的防止 SQL 注入， 提高系统安全性。

#### 通常一个Xml 映射文件，都会写一个Dao 接口与之对应，
   
   *请问，这个Dao 接口的工作原理是什么？Dao 接口里的方法， 参数不同时，方法能重载吗？*
   
   Dao 接口即 Mapper 接口。接口的全限名，就是映射文件中的 namespace 的值； 接口的方法名， 就是映射文件中 Mapper 的 Statement 的 id 值； 接口方法内的参数， 就是传递给 sql 的参数。
   
   Mapper 接口是没有实现类的，当调用接口方法时，接口全限名+方法名拼接字符串作为 key 值， 可唯一定位一个 MapperStatement。在 Mybatis 中， 每一个
   <select>、<insert>、<update>、<delete>标签，   都会被解析为一个MapperStatement 对象。
   
   举例： com.mybatis3.mappers.StudentDao.findStudentById， 可以唯一 找 到 namespace 为 com.mybatis3.mappers.StudentDao 下 面 id 为findStudentById 的 MapperStatement 。
   
   Mapper 接口里的方法，是不能重载的，因为是使用 全限名+方法名  的保存和寻找策略。Mapper 接口的工作原理是 JDK 动态代理， Mybatis 运行时会使用 JDK 动态代理为 Mapper 接口生成代理对象 proxy， 代理对象会拦截接口方法， 转而执行 MapperStatement 所代表的 sql， 然后将 sql 执行结果返回。
   
#### Mybatis 是如何进行分页的？分页插件的原理是什么？
   
   Mybatis 使用 RowBounds 对象进行分页， 它是针对 ResultSet 结果集执行的内存分页，而非物理分页。可以在 sql 内直接书写带有物理分页的参数来完成物理分页功能， 也可以使用分页插件来完成物理分页。
   
   分页插件的基本原理是使用 Mybatis 提供的插件接口， 实现自定义插件， 在插件的拦截方法内拦截待执行的 sql，然后重写 sql，根据 dialect 方言，添加对应的物理分页语句和物理分页参数。

#### 为什么说 Mybatis 是半自动 ORM 映射工具？它与全自动的区别在哪里？
   
   Hibernate 属于全自动 ORM 映射工具， 使用 Hibernate 查询关联对象或者关联集合对象时， 可以根据对象关系模型直接获取， 所以它是全自动的。而 Mybatis 在查询关联对象或关联集合对象时，需要手动编写 sql 来完成，所以，称之为半自动 ORM 映射工具。

#### Mybatis 是否支持延迟加载？如果支持，它的实现原理是什么？
   
   Mybatis 仅支持 association 关联对象和 collection 关联集合对象的延迟加载， association 指的就是一对一， collection 指的就是一对多查询。在 Mybatis 配置文件中， 可以配置是否启用延迟加载  lazyLoadingEnabled=true|false。
   
   它的原理是， 使用 CGLIB 创建目标对象的代理对象， 当调用目标方法时， 进入拦截器方法， 比如调用 a.getB().getName()， 拦截器 invoke()方法发现 a.getB()是null 值， 那么就会单独发送事先保存好的查询关联 B 对象的 sql， 把 B 查询上来， 然后调用 a.setB(b)，于是 a 的对象 b 属性就有值了，接着完成 a.getB().getName()方法的调用。这就是延迟加载的基本原理。
   
   当然了， 不光是 Mybatis， 几乎所有的包括 Hibernate， 支持延迟加载的原理都是一样的。

#### Mybatis 的一级、二级缓存:  
   
   一级缓存: 基于 PerpetualCache 的 HashMap 本地缓存， 其存储作用域为Session， 当 Session flush  或  close  之后， 该  Session  中的所有  Cache  就将清空， 默认打开一级缓存。
   
   二级缓存与一级缓存其机制相同，默认也是采用 PerpetualCache，HashMap 存储， 不同在于其存储作用域为 Mapper(Namespace)， 并且可自定义存储源， 如 Ehcache。默认不打开二级缓存， 要开启二级缓存， 使用二级缓存属性类需要实现 Serializable 序列化接口(可用来保存对象的状态),可在它的映射文件中配置
   <cache/> ；
   
   对于缓存数据更新机制， 当某一个作用域(一级缓存 Session/二级缓存
   Namespaces)的进行了 C/U/D 操作后，默认该作用域下所有 select 中的缓存将被 clear 。
   
#### 什么是 MyBatis 的接口绑定？有哪些实现方式？
   
   接口绑定，就是在 MyBatis 中任意定义接口,然后把接口里面的方法和 SQL 语句绑定, 我们直接调用接口方法就可以,这样比起原来了 SqlSession 提供的方法我们可以有更加灵活的选择和设置。
   
   接口绑定有两种实现方式,一种是通过注解绑定， 就是在接口的方法上面加上@Select、@Update 等注解， 里面包含 Sql 语句来绑定； 另外一种就是通过 xml 里面写 SQL 来绑定,
    在这种情况下,要指定 xml 映射文件里面的 namespace 必须为接口的全路径名。当 Sql 语句比较简单时候,用注解绑定, 当 SQL 语句比较复杂时候,用 xml 绑定,一般用 xml 绑定的比较多。

#### 简述 Mybatis 的插件运行原理，以及如何编写一个插件。
   
   Mybatis 仅可以编写针对 ParameterHandler、ResultSetHandler、StatementHandler、Executor 这 4 种接口的插件， Mybatis 使用 JDK 的动态代理， 
   为需要拦截的接口生成代理对象以实现接口方法拦截功能， 每当执行这 4 种接口对象的方法时，就会进入拦截方法，具体就是 InvocationHandler 的 invoke() 方法， 当然， 只会拦截那些你指定需要拦截的方法。
   
   编写插件： 实现 Mybatis 的 Interceptor 接口并复写 intercept()方法， 然后在给插件编写注解， 指定要拦截哪一个接口的哪些方法即可， 记住， 别忘了在配置文件中配置你编写的插件。
   