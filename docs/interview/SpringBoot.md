### SpringBoot 面试题

#### 1. 什么是SpringBoot ?
   
   SpringBoot 是Spring 开源组织下的子项目，是Spring 组件一站式解决方案，是快速开发脚手架，主要是简化了使用Spring 的难度,
   减省了繁重的配置，提供各种启动器，开发者能快速上手。

#### 2. SpringBoot 有哪些优点？
   
   1. 容易上手，提高开发效率，为spring 开发提供一个更快、更广泛的入门体验。
   
   2. 开箱即用，远离繁琐的配置。
   
   3. 提供了一系列大型项目通用的非业务性功能，例如内嵌服务器、安全管理、运行数据监控、运行状况检查和外部化配置等。
   
   4. 没有大码生成，也不需要XML 配置。
   
   5. 避免大量的Maven 导入和各种版本冲突。
   
#### 3. Spring Boot 自动配置原理是什么？
   
   注解@EnableConfiguration,@Configuration,@ConditionalOnClass 就是自动配置的核心
   
   @EnableAutoConfiguration 给容器导入META-INF/spring.factories 里定义的自动配置类。
   
   刷选有效的自动配置类。
   
   每一个自动配置类结合对应的xxxProperties.java 读取配置文件进行自动配置功能。
   
#### 4. 你如何理解Spring Boot 配置加载顺序?
   
   在Spring Boot 里面，可以使用以下几种方式来加载配置.
   
   1. properties 文件。
   
   2. YAML 文件.
   
   3. 系统环境变量。
   
   4. 命令行参数
   
#### 5. spring boot 核心配置文件是什么？ bootstrap.properties 和 application.properties 有何区别？ 

   bootstrap (.yml 或者 .properties) : bootstrap 由父ApplicationContext 加载的，比application 优先加载，
   配置在应用程序上下文的引导阶段生效。一般来说我们在Spring Cloud Config 或者 Nacos 中会用到，且bootstrap 里面的属性不能被覆盖
   
   application (.yml 或者 .properties): 由ApplicationContext 加载，用于Spring Boot 项目的自动化配置。

#### 6. springBoot 中常用注解 及其底层实现 

   1. @SpringBootApplication 注解，这个注解标识了一个 springBoot 工程，它实际上是另外是三个注解的组合： 

      a. @SpringBootConfiguration: 这个注解实际上就是 一个 @Configuration 注解，表示启动类也是一个配置类

      b. @EnableAutoConfiguration : 向 spring 容器中导入 一个 Selector ，用来加载 classpath 下 springFactories 中所定义的自动配置类，将这些自动配置类 加载为配置Bean 

      c. @ComponentScan： 标识扫描路径，因为默认是没有配置实际扫描路径的，所以springboot 扫描的路径是启动类所在的当前目录。 

   2. @Bean 注解 ，用来定义Bean ,类似于 xml 中的 <Bean> 标签，spring 在启动的时候，会对 加了 @Bean 注解的方法进行解析，将方法的名字做为beanName ,并通过执行方法得到bean; 

   3. @Controller @Service @ResponseBody @Autowired 等等。

#### springboot 是如何启动tomcat 的 

   1. 首先，springboot 在启动时会先创建spring 容器。

   2. 在创建spring 容器过程中，会利用 @ConditionalClass 技术来判断当前 classpath 中是否存在 tomcat 依赖，如果存在 则会生成一个启动Tomcat 的 Bean 

   3. spring 容器创建完之后，就会 获取 启动 Tomcat 的Bean ,并 创建 Tomcat 对象，绑定端口，然后启动 Tomcat 