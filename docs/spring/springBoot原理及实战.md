### 1. 自动配置实现原理

Spring Boot 自动配置，顾名思义，是希望能够自动配置，将我们从配置的苦海中解脱出来。那么既然要自动配置，它需要解三个问题：

- 创建哪些 **Bean** :  配置类  @Configuation
- 满足什么样的**条件**： 条件注解
- 创建 Bean的**属性**：配置属性

比如负责创建内嵌的 Tomcat、Jetty 等等 Web 服务器的配置类EmbeddedWebServerFactoryCustomizerAutoConfiguration。

```java
@Configuration     //配置类
@ConditionalOnWebApplication   //条件注解,表示当前配置类需要在当前项目是 Web 项目的条件下才能生效。
@EnableConfigurationProperties(ServerProperties.class)    // 配置属性
public class EmbeddedWebServerFactoryCustomizerAutoConfiguration{
    @Configuration
    @ConditionalOnClass({ Tomcat.class, UpgradeProtocol.class })  //  条件注解,表示当前配置类需要在当前项目有指定类的条件下才能生效
    public static class TomcatWebServerFactoryCustomizerConfiguration {
    
       @Bean
       public TomcatWebServerFactoryCustomizer tomcatWebServerFactoryCustomizer(
             Environment environment, ServerProperties serverProperties) {
          return new TomcatWebServerFactoryCustomizer(environment, serverProperties);
       }    
    }
}
```

#### 1.1 配置类

##### 1.1.1 @Configuration

在 Spring3.0 开始，Spring 提供了 JavaConfiguartion 的方式，允许我们使用 Java 代码的方式，进行 Spring Bean 的创建。

```java
@Configuration
public class AppConfig {

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setPort(10000);
        return factory;
    }
}
```

##### 1.1.2 自动配置类

Spring Boot 的 spring-boot-autoconfigure 项目，提供了大量框架的自动配置类。

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard.png)

​	通过 SpringApplication#run(Class<?> primarySource, String... args) 方法，启动 Spring Boot 应用的时候，有个非常重要的组件 SpringFactoriesLoader 类，会读取 META-INF 目录下的 spring.factories 文件，获得每个框架定义的需要自动配置的配置类。**所以自动配置只是 Spring Boot 基于spring.factories的一个拓展点 EnableAutoConfiguration。**

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard-1592807215767.png)

因为 spring-boot-autoconfigure 项目提供的是它选择的主流框架的自动配置，所以其它框架需要自己实现。例如，Dubbo 通过 dubbo-spring-boot-project 项目，提供 Dubbo 的自动配置。如下图所示：

![image-20200622142902788](SpringBoot自动配置实战及其原理剖析.assets/image-20200622142902788.png)

##### 1.1.3 Spring SPI机制

​	SPI 全称为 Service Provider Interface，是一种服务发现机制。SPI 的本质是将接口实现类的全限定名配置在文件中，并由服务加载器读取配置文件，加载实现类。这样可以在运行时，动态为接口替换实现类。正因此特性，我们可以很容易的通过 SPI 机制为我们的程序提供拓展功能。SPI 机制在第三方框架中也有所应用，比如 Dubbo 就是通过 SPI 机制加载所有的组件。

​	在springboot的自动装配过程中，最终会加载META-INF/spring.factories文件，而加载的过程是由**SpringFactoriesLoader**加载的。从CLASSPATH下的每个Jar包中搜寻所有META-INF/spring.factories配置文件，然后将解析properties文件，找到指定名称的配置后返回。

```markdown
org.springframework.core.io.support.SpringFactoriesLoader#loadFactoryNames
```

#### 1.2 条件注解

在 Spring3.1 版本时，为了满足不同环境注册不同的 Bean ，引入了 @Profile 注解。切换环境只需启动时配置虚拟机参数 **-Dspring.profiles.active**

```java
@Configuration
public class DataSourceConfiguration {

    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        // TODO 
    }

    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        // TODO
    }
}
```

在 Spring4 版本时，提供了 @Conditional 注解，用于声明在配置类或者创建 Bean 的方法上，表示需要满足指定条件才能生效

```java
@Configuration
public class AppConfig {
  @Bean
  public Cat cat(){
     return new Cat();
  }
  
  
  @Bean
  @Conditional(value = MyConditional.class)
  public Fox fox(){
     return new Fox()
  }
}


public class MyConditional implements Condition {
   @Override
   public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      if(context.getBeanFactory().containsBean("cat"))
         return true;
      return false;
   }
}
```

Spring Boot 进一步增强，提供了常用的条件注解：

- @ConditionalOnNotWebApplication：当前项目不是 Web 项目的条件下
- @ConditionalOnWebApplication：当前项目是 Web项目的条件下

- @ConditionalOnBean：当容器里有指定 Bean 的条件下

- @ConditionalOnMissingBean：当容器里没有指定 Bean 的情况下
- @ConditionalOnSingleCandidate：当指定 Bean 在容器中只有一个，或者虽然有多个但是指定首选 Bean
- @ConditionalOnClass：当类路径下有指定类的条件下
- @ConditionalOnMissingClass：当类路径下没有指定类的条件下
- @ConditionalOnProperty：指定的属性是否有指定的值
- @ConditionalOnResource：类路径是否有指定的值
- @ConditionalOnExpression：基于 SpEL 表达式作为判断条件
- @ConditionalOnJava：基于 Java 版本作为判断条件
- @ConditionalOnJndi：在 JNDI 存在的条件下差在指定的位置

#### 1.3 配置属性

使用 @EnableConfigurationProperties 注解，让 ServerProperties 配置属性类生效

```markdown
@EnableConfigurationProperties(ServerProperties.class)
```

通过 @ConfigurationProperties 注解，声明将 server 前缀的配置项，设置ServerProperties 配置属性类中

```java
@ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
public class ServerProperties {

   /**
    * Server HTTP port.
    */
   private Integer port;

   /**
    * Network address to which the server should bind.
    */
   private InetAddress address;
```

Spring Boot 约定读取 application.yaml、application.properties 等配置文件，从而实现创建 Bean 的自定义属性配置，甚至可以搭配 @ConditionalOnProperty 注解来取消 Bean 的创建。

```markdown
org.springframework.boot.env.PropertySourceLoader
org.springframework.boot.env.PropertiesPropertySourceLoader
org.springframework.boot.env.YamlPropertySourceLoader
```

#### 1.4 Starter依赖

##### 1.4.1 内置 Starter

我们在使用 Spring Boot 时，并不会直接引入 [spring-boot-autoconfigure](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-autoconfigure) 依赖，而是使用 Spring Boot 内置提供的 Starter 依赖。

例如，我们想要使用 SpringMVC 时，引入的是 spring-boot-starter-web 依赖。这是为什么呢？

因为 Spring Boot 提供的自动配置类，基本都有 @ConditionalOnClass 条件注解，判断我们项目中存在指定的类，才会创建对应的 Bean。而拥有指定类的前提，一般是需要我们引入对应框架的依赖。因此，在我们引入 spring-boot-starter-web 依赖时，它会帮我们自动引入相关依赖，从而保证自动配置类能够生效，创建对应的 Bean。

```markdown
org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
```

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard-1592814316370.png)

##### 1.4.2 自定义Starter

1.引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-autoconfigure</artifactId>
</dependency>

<!-- Spring Boot 配置处理器 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
</dependency>
```

2.实现自动配置类 WlzServerAutoConfiguration

```java
@Configuration
@ConditionalOnClass(WlzServer.class)
@EnableConfigurationProperties(WlzServerProperties.class)
public class WlzServerAutoConfiguration {

   @Autowired
   private WlzServerProperties properties;

   @Bean
   @ConditionalOnClass(HttpServer.class)
   @ConditionalOnProperty(prefix = "wlz.server", value = "enabled",havingValue = "true")
   WlzServer wlzServer (){
      return new WlzServer(properties.getPort());
   }

}


@ConfigurationProperties(prefix = "wlz.server")
public class WlzServerProperties {


   private Integer port = 8000;

   public Integer getPort() {
      return port;
   }

   public void setPort(Integer port) {
      this.port = port;
   }
}

public class WlzServer {


   private Integer port;

   public WlzServer(Integer port) {
      this.port = port;
   }

   public HttpServer create()  throws IOException{
      HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
      server.start();
      System.out.println("启动服务器成功，端口为:"+port);
      return server;

   }
}
```

3.创建**spring.factories**文件

​		在 resources 目录下创建，创建 META-INF 目录，然后在该目录下创建 spring.factories文件，添加自动化配置类为 **FoxServerAutoConfiguration**。

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  bat.ke.qq.com.FoxServerAutoConfiguration
```

4. 测试

application.properties中配置

```properties
fox.server.enabled=true
fox.server.port=8001
```

```java
@SpringBootApplication
@RestController
public class DemoApplication {

   public static void main(String[] args) {
      SpringApplication.run(DemoApplication.class, args);
   }

   @Autowired
   private WlzServer wlzServer;


   @RequestMapping("/")
   public String sayHello()  {
      try {
         wlzServer.create();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return "say hello";
   }
}
```

### 2. SpringBoot 自动配置原理源码分析

自动配置 @SpringBootApplication：

![](SpringBoot自动配置实战及其原理剖析.assets/springbootApplication.png)
![](SpringBoot自动配置实战及其原理剖析.assets/springbootApplication.png)

自动配置如何注册AutoConfiguration：

![](SpringBoot自动配置实战及其原理剖析.assets/autoconfiguration.png)

**@SpringBootApplication**

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard.png)

**@Inherited**

JAVA自带的注解，使用此注解声明出来的自定义注解，在使用此自定义注解时，如果注解在类上面时，子类会自动继承此注解，否则的话，子类不会继承此注解。

**@SpringBootConfiguration**

Spring自定义注解，标记这是一个 Spring Boot 配置类

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard.png)

**@ComponentScan**

Spring 自定义的注解，扫描指定路径下的 Component

- basePackages：指定扫描的包，basePackageClasses：指定扫描的类

- useDefaultFilters：取消默认filter设置，默认Filter自动扫描包下的Component，取消相当于禁用某个扫描

- @ComponentScan.Filter：指定Filter 规则，可用于excludeFilters 排除Filters ，includeFilters 包含Filters

**@EnableAutoConfiguration**

Spring Boot 自定义的注解，用于开启自动配置功能，是 spring-boot-autoconfigure 项目最核心的注解。

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard.png)

- **@AutoConfigurationPackage** 注解，将启动类所在的package作为自动配置的package ，会向容器注册beanName为org.springframework.boot.autoconfigure.AutoConfigurationPackages的bean。
- **@Import** 注解，可用于资源的导入
- **AutoConfigurationImportSelector** ，导入自动配置相关的资源

**AutoConfigurationImportSelector**
```
protected List<String> getCandidateConfigurations(AnnotationMetadata metadata,
      AnnotationAttributes attributes) {
 //加载指定类型 EnableAutoConfiguration 对应的，在 `META-INF/spring.factories` 里的配置类
 // SpringFactoriesLoaderFactoryClass: org.springframework.boot.autoconfigure.EnableAutoConfiguration
   List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
         getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader());
   Assert.notEmpty(configurations,
         "No auto configuration classes found in META-INF/spring.factories. If you "
               + "are using a custom packaging, make sure that file is correct.");
   return configurations;
}
```

```
protected AutoConfigurationEntry getAutoConfigurationEntry(
      AutoConfigurationMetadata autoConfigurationMetadata,
      AnnotationMetadata annotationMetadata) {
   //是否开启自动配置  spring.boot.enableautoconfiguration 默认开启       
   if (!isEnabled(annotationMetadata)) {
      return EMPTY_ENTRY;
   }
   // 返回@EnableAutoConfiguration 注解的属性 exclude和excludeName
   AnnotationAttributes attributes = getAttributes(annotationMetadata);
   List<String> configurations = getCandidateConfigurations(annotationMetadata,
         attributes);
   // 移除重复的配置类      
   configurations = removeDuplicates(configurations);
   //获得需要排除的配置类
   //注解上的exclude和excludeName 以及 spring.autoconfigure.exclude
   Set<String> exclusions = getExclusions(annotationMetadata, attributes);
   checkExcludedClasses(configurations, exclusions);
   // 从 configurations 中，移除需要排除的配置类
   configurations.removeAll(exclusions);
   // 根据条件（Condition），过滤掉不符合条件的配置类
   // AutoConfigurationImportFilter   Auto Configuration Import Filters
   configurations = filter(configurations, autoConfigurationMetadata);
   // 触发自动配置类引入完成的事件
   // AutoConfigurationImportListener   Auto Configuration Import Listeners
   fireAutoConfigurationImportEvents(configurations, exclusions);
   return new AutoConfigurationEntry(configurations, exclusions);
}
```  

### 3. Spring Boot启动 实现原理 

#### 3.1 SpringBoot 是如何通过jar包启动的

  得益于SpringBoot的封装，我们可以只通过jar -jar一行命令便启动一个web项目。再也不用操心搭建tomcat等相关web 容器。 

##### 3.1.1 java -jar 做了什么? 

   先要弄清楚java -jar命令做了什么，在oracle官网找到了该命令的描述:

       If the -jar option is specified, its argument is the name of the JAR file containing class and resource files for the application. The startup class must be indicated by the Main-Class manifest header in its source code.

   翻译过来就是: 使用 -jar 参数时，后面的参数是 jar 文件名 (demo.jar)；
   
   该jar 文件中包含的是class 和 资源文件；

   在 manifest 文件中有 Main-Class 的定义; 

   Main-Class 的源码中指定整个应用的启动类 

   总结: java -jar 会去找 jar 中 manifest 文件，在那里找到真正的启动类 

##### 3.1.2 疑惑 

   在MANIFEST.MF 文件中 有这么一行 内容: 

```
Start-Class: com.wlz.DemoApplication
```

前面的java官方文档中，只提到过Main-Class ，并没有提到Start-Class; Start-Class的值是com.wlz.DemoApplication，
这是我们的java代码中的唯一类，也只真正的应用启动类; 所以问题就来了:理论上看，执行java -jar命令时JarLauncher类会被执行，但实际上是com.tulingxueyuan.Application被执行了，这其 中发生了什么呢?为什么要这么做呢?

Java没有提供任何标准的方式来加载嵌套的jar文件(即，它们本身包含在jar中的jar文件)。

##### 3.1.3 jar 包的打包插件及核心方法 

Spring Boot项目的pom.xml文件中默认使用如下插件进行打包:

```xml
 <plugin>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

执行maven clean package之后，会生成两个文件:

```
springBoot-demo-0.0.1-SNAPSHOT.jar
springBoot-demo-0.0.1-SNAPSHOT.jar.original
```

  spring-boot-maven-plugin 项 目 存 在 于 spring-boot-tools 目 录 中 。 spring-boot-maven-plugin 默 认 有 5 个 goals : repackage、run、start、stop、build-info。在打包的时候默认使用的是repackage。

  spring-boot-maven-plugin的repackage能够将mvn package生成的软件包，再次打包为可执行的软件包，并将mvn package生成的软件包重命名为*.original。

spring-boot-maven-plugin的repackage在代码层面调用了RepackageMojo的execute方法，而在该方法中又调用了 repackage方法。repackage方法代码及操作解析如下:

```
private void repackage() throws MojoExecutionException { // maven生成的jar，最终的命名将加上.original后缀
   Artifact source = getSourceArtifact();
   // 最终为可执行jar，即fat jar
   File target = getTargetFile();
   // 获取重新打包器，将maven生成的jar重新打包成可执行jar
   Repackager repackager = getRepackager(source.getFile());
   // 查找并过滤项目运行时依赖的jar
   Set<Artifact> artifacts = filterDependencies(this.project.getArtifacts(),
   getFilters(getAdditionalFilters()));
   // 将artifacts转换成libraries
   Libraries libraries = new ArtifactsLibraries(artifacts, this.requiresUnpack, getLog());
   try{
      // 获得Spring Boot启动脚本
      LaunchScript launchScript = getLaunchScript();
      // 执行重新打包，生成fat jar
      repackager.repackage(target, libraries, launchScript);
   }catch (IOException ex) {
       throw new MojoExecutionException(ex.getMessage(), ex);
   }
   // 将maven生成的jar更新成.original文件
   updateArtifact(source, target, repackager.getBackupFile());
}
```

  执行以上命令之后，便生成了打包结果对应的两个文件。下面针对文件的内容和结构进行一探究竟。

##### 3.1.4 jar 包目录结构

首先来看看jar的目录结构，都包含哪些目录和文件，解压jar包可以看到如下结构:

![jar包目录](SpringBoot自动配置实战及其原理剖析.assets/jar包目录.png)

##### 3.1.5 META-INF 中 MANIFEST.MF 内容 

```xml
Manifest-Version: 1.0
Implementation-Title: springBoot-demo
Implementation-Version: 0.0.1-SNAPSHOT
Start-Class: com.wlz.DemoApplication
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
Build-Jdk-Spec: 1.8
Spring-Boot-Version: 2.1.5.RELEASE
Created-By: Maven Archiver 3.4.0
Main-Class: org.springframework.boot.loader.JarLauncher
```

  可以看到有Main-Class是org.springframework.boot.loader.JarLauncher ，这个是jar启动的Main函数。 

  还有一个Start-Class是com.wlz.DemoApplication，这个是我们应用自己的Main函数。

##### 3.1.6 Archive 的概念

在继续了解底层概念和原理之前，我们先来了解一下Archive的概念: 

    archive即归档文件，这个概念在linux下比较常见。 
    通常就是一个tar/zip格式的压缩包。
    jar是zip格式。

 SpringBoot 抽 象 了 Archive 的 概 念 ， 一 个 Archive 可 以 是 jar ( JarFileArchive ) ， 可 以 是 一 个 文 件 目 录 (ExplodedArchive)，可以抽象为统一访问资源的逻辑层。 

 SpringBoot定义了一个接口用于描述资源，也就是org.springframework.boot.loader.archive.Archive。该接口有两个 实现，分别是org.springframework.boot.loader.archive.ExplodedArchive和 org.springframework.boot.loader.archive.JarFileArchive。前者用于在文件夹目录下寻找资源，后者用于在jar包环境 下寻找资源。而在SpringBoot打包的fatJar中，则是使用后者。

  JarFile:对jar包的封装，每个JarFileArchive都会对应一个JarFile。JarFile被构造的时候会解析内部结构，去获取jar包里的 各个文件或文件夹，这些文件或文件夹会被封装到Entry中，也存储在JarFileArchive中。如果Entry是个jar，会解析成 JarFileArchive。

  比如一个JarFileArchive对应的URL为:

```
jar:file:/Users/format/Develop/gitrepository/springboot‐analysis/springboot‐executable‐jar/target/exeutable‐jar‐1.0‐SNAPSHOT.jar!/
```

它对应的JarFile为: 

```
/Users/format/Develop/gitrepository/springboot‐analysis/springboot‐executable‐jar/target/exeutable‐jar‐1.0‐SNAPSHOT.jar!/
```

这个JarFile有很多Entry，比如: 

```
META-INF/
META-INF/MANIFEST.MF
spring/
spring/study/
....
spring/study/executablejar/ExecutablejarApplication.class
....
```

JarFileArchive内部的一些依赖jar对应的URL(SpringBoot使用org.springframework.boot.loader.jar.Handler处理器来处理这些 URL):

```
jar:file:/Users/Format/Develop/gitrepository/springboot‐analysis/springboot‐executable‐jar/target/exe utable‐jar‐1.0‐SNAPSHOT.jar!/lib/spring‐boot‐starter‐web‐1.3.5.RELEASE.jar!/
```

```
jar:file:/Users/Format/Develop/gitrepository/springboot‐analysis/springboot‐executable‐jar/target/exe utable‐jar‐1.0‐SNAPSHOT.jar!/lib/spring‐boot‐loader‐ 1.3.5.RELEASE.jar!/org/springframework/boot/loader/JarLauncher.class
```

我们看到如果有jar包中包含jar，或者jar包中包含jar包里面的class文件，那么会使用 !/ 分隔开，这种方式只有 org.springframework.boot.loader.jar.Handler能处理，它是SpringBoot内部扩展出来的一种URL协议。

##### 3.1.7 JarLauncher 

从MANIFEST.MF可以看到Main函数是JarLauncher，下面来分析它的工作流程。JarLauncher类的继承结构是: 

````

/**
 * {@link Launcher} for JAR based archives. This launcher assumes that dependency jars are
 * included inside a {@code /BOOT-INF/lib} directory and that application classes are
 * included inside a {@code /BOOT-INF/classes} directory.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
public class JarLauncher extends ExecutableArchiveLauncher {
public abstract class ExecutableArchiveLauncher extends Launcher {
````

  按照定义，JarLauncher 可以加载内部 /BOOT-INF/lib 下的jar 以及 /BOOT-INF/classes 下的class ,其实JarLauncher实 现很简单:

```java
public class JarLauncher extends ExecutableArchiveLauncher {

	static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";

	static final String BOOT_INF_LIB = "BOOT-INF/lib/";

	public JarLauncher() {
	}

	protected JarLauncher(Archive archive) {
		super(archive);
	}

	@Override
	protected boolean isNestedArchive(Archive.Entry entry) {
		if (entry.isDirectory()) {
			return entry.getName().equals(BOOT_INF_CLASSES);
		}
		return entry.getName().startsWith(BOOT_INF_LIB);
	}

	public static void main(String[] args) throws Exception {
		new JarLauncher().launch(args);
	}

}

```

 其主入口新建了JarLauncher并调用父类Launcher中的launch方法启动程序。在创建JarLauncher时，父类 ExecutableArchiveLauncher找到自己所在的jar，并创建archive。 

 JarLauncher继承于org.springframework.boot.loader.ExecutableArchiveLauncher。该类的无参构造方法最主要的功 能就是构建了当前main方法所在的FatJar的JarFileArchive对象。下面来看launch方法。该方法主要是做了2个事情:

    (1)以FatJar为file作为入参，构造JarFileArchive对象。获取其中所有的资源目标，取得其Url，将这些URL作为参数， 构建了一个URLClassLoader。
   
    (2)以第一步构建的ClassLoader加载MANIFEST.MF文件中Start-Class指向的业务类，并且执行静态方法main。进而 启动整个程序。

```java
public abstract class ExecutableArchiveLauncher extends Launcher {

   private final Archive archive;

   public ExecutableArchiveLauncher() {
      try {
         this.archive = createArchive();
      }
      catch (Exception ex) {
         throw new IllegalStateException(ex);
      }
   }
}

public abstract class Launcher {
    
	protected final Archive createArchive() throws Exception {
		ProtectionDomain protectionDomain = getClass().getProtectionDomain();
		CodeSource codeSource = protectionDomain.getCodeSource();
		URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
		String path = (location != null) ? location.getSchemeSpecificPart() : null;
		if (path == null) {
			throw new IllegalStateException("Unable to determine code source archive");
		}
		File root = new File(path);
		if (!root.exists()) {
			throw new IllegalStateException(
					"Unable to determine code source archive from " + root);
		}
		return (root.isDirectory() ? new ExplodedArchive(root)
				: new JarFileArchive(root));
	}

}
```

在 Launcher 的 launch 方 法 中 ， 通 过 以 上 archive 的 getNestedArchives 方 法 找 到 /BOOT-INF/lib 下 所 有 jar 及 /BOOT- INF/classes目录所对应的archive，通过这些archives的url生成LaunchedURLClassLoader，并将其设置为线程上下文 类加载器，启动应用。

至此，才执行我们应用程序主入口类的main方法，所有应用程序类文件均可通过/BOOT-INF/classes加载，所有依赖的 第三方jar均可通过/BOOT-INF/lib加载。

##### 3.1.8 springboot 的jar 应用启动流程总结 

   1. spring boot 应用打包之后，生成一个 Fat jar ,包含了应用依赖的jar 包和spring boot loader 相关的包。 

   2. Fat jar 的启动Main 函数是 JarLauncher ，它负责创建一个 LauncherURLClassLoader 来加载/lib 下面的jar, 并以一个新线程启动应用的 Main 函数。

##### 3.1.9 总结

  JarLauncher通过加载BOOT-INF/classes目录及BOOT-INF/lib目录下jar文件，实现了fat jar的启动。
  
  SpringBoot通过扩展JarFile、JarURLConnection及URLStreamHandler，实现了jar in jar中资源的加载。 
  
  SpringBoot通过扩展URLClassLoader–LauncherURLClassLoader，实现了jar in jar中class文件的加载。 
  
  WarLauncher通过加载WEB-INF/classes目录及WEB-INF/lib和WEB-INF/lib-provided目录下的jar文件，实现了war文 件的直接启动及web容器中的启动。

#### 3.2 SpringBoot是如何启动Spring容器源码: 

##### 3.2.1 SpringBoot 事假监听器发布顺序: 

  1.ApplicationStartingEvent在运行开始时发送，但在进行任何处理之前(侦听器和初始化程序的注册除外)发送。
 
  2.在创建上下文之前，将发送ApplicationEnvironmentPreparedEvent。

  3.准备ApplicationContext并调用ApplicationContextInitializers之后，将发送ApplicationContextInitializedEv nt。

  4.读取完配置类后发送ApplicationPreparedEvent。

##### 3.2.2 调用SpringApplication.run启动springboot应用 

SpringApplication.run(DemoApplication.class, args); 

##### 3.2.3 使用自定义SpringApplication进行启动 

```
  public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
        return (new SpringApplication(primarySources)).run(args);
    }
```

##### 3.2.4 创建 SpringApplication 

```java
public class SpringApplication {
    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        this.sources = new LinkedHashSet();
        this.bannerMode = Mode.CONSOLE;
        this.logStartupInfo = true;
        this.addCommandLineProperties = true;
        this.addConversionService = true;
        this.headless = true;
        this.registerShutdownHook = true;
        this.additionalProfiles = new HashSet();
        this.isCustomEnvironment = false;
        this.resourceLoader = resourceLoader;
        Assert.notNull(primarySources, "PrimarySources must not be null");
        // 将启动列放入 primarySources
        this.primarySources = new LinkedHashSet(Arrays.asList(primarySources));
        // 根据 classpath 下的类，推算当前web 应用类型 (webFlux、servlet)
        this.webApplicationType = WebApplicationType.deduceFromClasspath();
        // 就是去 spring.factories 中去获取 所有key:org.springframework.context.ApplicationContextInitializer 
        this.setInitializers(this.getSpringFactoriesInstances(ApplicationContextInitializer.class));
        // 就是去 spring.factories 中去获取 所有key:org.springframework.context.ApplicationListener
        this.setListeners(this.getSpringFactoriesInstances(ApplicationListener.class));
        // 根据main 方法推算出mainApplicationClass 
        this.mainApplicationClass = this.deduceMainApplicationClass();
    }
}
```

###### 总结 

      1. 获取启动类
      2.获取web应用类型
      3.读取了对外扩展的ApplicationContextInitializer ,ApplicationListener 4. 根据main推算出所在的类
   
  就是去初始化了一些信息

##### 3.2.5 启动 run 
```java
public class SpringApplication {
   public ConfigurableApplicationContext run(String... args) {
       // 用来记录当前springboot 启动耗时
      StopWatch stopWatch = new StopWatch();
      // 记录启动开始时间
      stopWatch.start();
      // 它是任何spring 上下文的接口，所以可以接收任何ApplicationContext 实现 
      ConfigurableApplicationContext context = null;
      Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
      // 开启 headless 模式 
      configureHeadlessProperty();
      // 去spring.factories 中读取了 SpringApplicationRunListener 的组件，就是用来发布事件或者运行监听器 
      SpringApplicationRunListeners listeners = getRunListeners(args);
      // 发布1. ApplicationStartingEvent 事件 ，在运行开始时发送 
      listeners.starting();
      try {
          // 根据命令行参数 实例化一个 ApplicationArguments 
         ApplicationArguments applicationArguments = new DefaultApplicationArguments(
                 args);
         // 预初始化环境，读取环境变量，读取配置文件信息(基于监听器）
         ConfigurableEnvironment environment = prepareEnvironment(listeners,
                 applicationArguments);
         // 忽略beaninfo 的bean 
         configureIgnoreBeanInfo(environment);
         // 打印 banner 横幅 
         Banner printedBanner = printBanner(environment);
         // 根据 webApplicationType 创建spring 上下文 
         context = createApplicationContext();
         exceptionReporters = getSpringFactoriesInstances(
                 SpringBootExceptionReporter.class,
                 new Class[]{ConfigurableApplicationContext.class}, context);
         // 预初始化spring 上下文 
         prepareContext(context, environment, listeners, applicationArguments,
                 printedBanner);
         // 加载 spring ioc 容器，由于使用 AnnotationConfigServletWebServerApplicationContext 启动的spring 容器所以springboot dui
         // 它做了扩展:
         // 加载自动配置类: invokeBeanFactoryPostProcessor ，创建 servlet 容器 onRefresh 
         refreshContext(context);
         afterRefresh(context, applicationArguments);
         stopWatch.stop();
         if (this.logStartupInfo) {
            new StartupInfoLogger(this.mainApplicationClass)
                    .logStarted(getApplicationLog(), stopWatch);
         }
         listeners.started(context);
         callRunners(context, applicationArguments);
      } catch (Throwable ex) {
         handleRunFailure(context, ex, exceptionReporters, listeners);
         throw new IllegalStateException(ex);
      }

      try {
         listeners.running(context);
      } catch (Throwable ex) {
         handleRunFailure(context, ex, exceptionReporters, null);
         throw new IllegalStateException(ex);
      }
      return context;
   }
}
```

###### 总结 

   1. 初始化springApplication 从 spring.factories 读取 litener , ApplicationContextInializer 
 
   2. 运行 run 方法。
 
   3. 读取环境变量 配置信息 

   4. 创建 springApplication 上下文: ServletWebServerApplicationContext 

   5. 预初始化上下文: 读取启动类 

   6. 调用 refresh 加载ioc 容器 

   7. 在这个过程中 springboot 会调用 很多监听器对外进行监听 



### 4. Spring Boot启动实现原理源码分析 

![](SpringBoot自动配置实战及其原理剖析.assets/springboot启动流程.png)

### 5. Spring Boot整合第三方框架实战

#### 5.1 整合jdbc

1. 导入的maven依赖  

   ```java
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-jdbc</artifactId>
   </dependency>
   
   <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
       <scope>runtime</scope>
       <version>5.1.47</version>
   </dependency>
   ```

2. 配置数据源

   ```yaml
   spring:
     datasource:
       # mysql8的驱动和url
       #driver-class-name: com.mysql.cj.jdbc.Driver
       #url: jdbc:mysql://localhost:3306/test?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=false
       driver-class-name: com.mysql.jdbc.Driver
       url: jdbc:mysql://localhost:3306/test
       username: root
       password: root
   ```

   

3. 测试

   ```java
   @SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
   @RunWith(SpringRunner.class)
   public class SpringBootJdbcApplicationTest {
       @Autowired
       private DataSource dataSource;
   
       @Test
       public void test(){
           // HikariDataSource
           System.out.println("自动配置数据源的类型:"+dataSource.getClass());
       }
   }
   ```

   

4. DataSource的自动配置

   ```java
   //org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration
   /**
    * Hikari DataSource configuration.    默认数据源配置
    */
   @Configuration
   @ConditionalOnClass(HikariDataSource.class)
   @ConditionalOnMissingBean(DataSource.class)
   @ConditionalOnProperty(name = "spring.datasource.type",
         havingValue = "com.zaxxer.hikari.HikariDataSource", matchIfMissing = true)
   static class Hikari {
   
      @Bean
      @ConfigurationProperties(prefix = "spring.datasource.hikari")
      public HikariDataSource dataSource(DataSourceProperties properties) {
         HikariDataSource dataSource = createDataSource(properties,
               HikariDataSource.class);
         if (StringUtils.hasText(properties.getName())) {
            dataSource.setPoolName(properties.getName());
         }
         return dataSource;
      }
   }
   
      /**
        * Generic DataSource configuration.   通用数据源配置，比如整合druid
        */
       @Configuration
       @ConditionalOnMissingBean(DataSource.class)
       @ConditionalOnProperty(name = "spring.datasource.type")
       static class Generic {
       
          @Bean
          public DataSource dataSource(DataSourceProperties properties) {
             return properties.initializeDataSourceBuilder().build();
          }
       
       }
   ```

   

5. jdbcTemplate自动配置

   ```java
   //org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
   
   @Bean
   @Primary
   @ConditionalOnMissingBean(JdbcOperations.class)
   public JdbcTemplate jdbcTemplate() {
      JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
      JdbcProperties.Template template = this.properties.getTemplate();
      jdbcTemplate.setFetchSize(template.getFetchSize());
      jdbcTemplate.setMaxRows(template.getMaxRows());
      if (template.getQueryTimeout() != null) {
         jdbcTemplate
               .setQueryTimeout((int) template.getQueryTimeout().getSeconds());
      }
      return jdbcTemplate;
   }
   ```

   测试

   ```java
   @Autowired
   private JdbcTemplate jdbcTemplate;
   
   @Test
   public void testJdbcTemplate() {
       List<Map<String,Object>> list = jdbcTemplate.queryForList(
       "select * from user");
       System.out.println(list.size());
   }
   ```

#### 5.2 整合druid

 引入依赖

```java
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.21</version>
</dependency>
```

定制一个druid的配置类

```java
@ConfigurationProperties(prefix = "spring.datasource.druid")
@Data
public class DruidDataSourceProperties {

    private String username;
    
    private String password;
    
    private String jdbcUrl;
    
    private String driverClassName;
    
    private Integer initialSize;

    private Integer maxActive;

    private Integer minIdle;

    private long maxWait;

    private boolean poolPreparedStatements;

    public String filters;

}

// 配置数据源监控，配置一个statViewSerlvet(后端管理) WebStatFilter sql监控
@Configuration
@EnableConfigurationProperties(value = DruidDataSourceProperties.class)
public class DruidDataSourceConfig {

    @Autowired
    private DruidDataSourceProperties druidDataSourceProperties;

    @Bean
    public DataSource dataSource() throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUsername(druidDataSourceProperties.getUsername());
        druidDataSource.setPassword(druidDataSourceProperties.getPassword());
        druidDataSource.setUrl(druidDataSourceProperties.getJdbcUrl());
        druidDataSource.setDriverClassName(druidDataSourceProperties.getDriverClassName());
        druidDataSource.setInitialSize(druidDataSourceProperties.getInitialSize());
        druidDataSource.setMinIdle(druidDataSourceProperties.getMinIdle());
        druidDataSource.setMaxActive(druidDataSourceProperties.getMaxActive());
        druidDataSource.setMaxWait(druidDataSourceProperties.getMaxWait());
        druidDataSource.setFilters(druidDataSourceProperties.getFilters());
        druidDataSourceProperties.setPoolPreparedStatements(druidDataSourceProperties.isPoolPreparedStatements());
        return druidDataSource;
    }

    /**
     * 配置druid管理后台的servlet
     * @return
     */
    @Bean
    public ServletRegistrationBean statViewSerlvet() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(),"/druid/*");
        Map<String,Object> initParameters = new HashMap<>();
        initParameters.put("loginUsername","admin");
        initParameters.put("loginPassword","123456");
        bean.setInitParameters(initParameters);
        return bean;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));

        Map<String,Object> initParams = new HashMap<>();
        initParams.put("exclusions","*.js,*.css,/druid/*");
        filterRegistrationBean.setInitParameters(initParams);
        return  filterRegistrationBean;
    }
}
```

配置application.yml

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      username: root
      password: root
      jdbcUrl: jdbc:mysql://localhost:3306/test
      driverClassName: com.mysql.jdbc.Driver
      initialSize: 5
      minIdle: 5
      maxActive: 20
      maxWait: 60000
      timeBetweenEvictionRunsMillis: 60000
      minEvictableIdleTimeMillis: 300000
      validationQuery: SELECT 1 FROM DUAL
      testWhileIdle: true
      testOnBorrow: false
      testOnReturn: false
      poolPreparedStatements: true
      filters: stat,wall
      maxPoolPreparedStatementPerConnectionSize: 20
      useGlobalDataSourceStat: true
      connectionProperties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=500
```

监控访问路径: http://localhost:8080/druid/

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard-1592815300540.png)

#### 5.3 整合mybaits

引入依赖

```yaml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
    <version>5.1.46</version>
</dependency>

<!-- mybatis自动配置依赖-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

编写Mapper接口

```java
public interface UserMapper {
    @Select("select * from user")
    List<User> list();
}
```

在启动类加上@MapperScan注解

```java
@SpringBootApplication
@MapperScan("bat.ke.qq.com.vipspringbootmybatis.mapper")
public class VipSpringBootMybatisApplication {

    public static void main(String[] args) {
        SpringApplication.run(VipSpringBootMybatisApplication.class, args);
    }
}
```

mybatis自动配置原理  

org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration

- 自动配置了 sqlSessionFactory，sqlSessionTemplate
- @Import({ AutoConfiguredMapperScannerRegistrar.**class** })

sql写在方法上配置方式：

方式一： 启动类上加@MapperScan，扫描mapper包

方式二： 每一个Mapper接口都添加@Mapper

sql写在配置文件上：

```yaml
#配置mybatis
mybatis:
    configuration:
        map-underscore-to-camel-case: true 开启驼峰命名
    mapper-locations: classpath:/mybatis/mapper/*.xml 指定配置文件的位置
```

#### 5.4 整合redis

SpringBoot2.x默认使用的是Lettuce。

关于jedis跟lettuce的区别：

- Lettuce 和 Jedis 的定位都是Redis的client，所以他们当然可以直接连接redis server。
- Jedis在实现上是直接连接的redis server，如果在多线程环境下是非线程安全的，这个时候只有使用连接池，为每个Jedis实例增加物理连接。
- Lettuce的连接是基于Netty的，连接实例（StatefulRedisConnection）可以在多个线程间并发访问，应为StatefulRedisConnection是线程安全的，所以一个连接实例（StatefulRedisConnection）就可以满足多线程环境下的并发访问，当然这个也是可伸缩的设计，一个连接实例不够的情况也可以按需增加连接实例。

1. 引入依赖

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-redis</artifactId>
   </dependency>
   
   <dependency>
       <groupId>org.apache.commons</groupId>
       <artifactId>commons-pool2</artifactId>
       <version>2.6.2</version>
   </dependency>
   ```

   配置原理：

   org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration 自动配置redisTemplate和stringRedisTemplate

2. 配置yaml ，对应RedisProperties配置类

   ```yaml
   spring:
     redis:
       # Redis开关/默认关闭
       enabled: true
       database: 0
       password:     #redis密码
       host: 192.168.3.14
       port: 6379
       lettuce:
         pool:
           max-active:  100 # 连接池最大连接数（使用负值表示没有限制）
           max-idle: 100 # 连接池中的最大空闲连接
           min-idle: 50 # 连接池中的最小空闲连接
           max-wait: 6000 # 连接池最大阻塞等待时间（使用负值表示没有限制）
       timeout: 1000
   ```

   使用redis 自动配置的默认的redisTemplate是使用jdk自带的序列化工具JdkSerializationRedisSerializer,通过redis 客户端工具看到的key，value是字节形式的

   ![img](https://note.youdao.com/yws/public/resource/ed92757780d920287266eb300bfca984/xmlnote/40CF62CA46EC44C68D7436AC126AC228/5648)

   自己配置一个RedisTemplate，替换序列化协议

   ```java
   @Configuration
   public class RedisConfig {
       /**
        * 配置自定义redisTemplate
        * @return
        */
       @Bean
       RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
           RedisTemplate<String, Object> template = new RedisTemplate<>();
           template.setConnectionFactory(redisConnectionFactory);
   
            //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
           Jackson2JsonRedisSerializer serializer =
                   new Jackson2JsonRedisSerializer(Object.class);
           ObjectMapper mapper = new ObjectMapper();
           mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
           mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
           serializer.setObjectMapper(mapper);
           template.setValueSerializer(serializer);
   
           //使用StringRedisSerializer来序列化和反序列化redis的key值
           template.setKeySerializer(new StringRedisSerializer());
           template.setHashKeySerializer(new StringRedisSerializer());
           template.setHashValueSerializer(serializer);
           template.afterPropertiesSet();
   
           return template;
       }
   }
   ```

#### 5.5 整合dubbo

**provider端**

1. 引入依赖

   ```xml
   <!-- 实现对 Dubbo 的自动化配置 -->
   <dependency>
       <groupId>org.apache.dubbo</groupId>
       <artifactId>dubbo</artifactId>
       <version>2.7.3</version>
   </dependency>
   <dependency>
       <groupId>org.apache.dubbo</groupId>
       <artifactId>dubbo-spring-boot-starter</artifactId>
       <version>2.7.3</version>
   </dependency>
   
   <!-- 使用 Zookeeper 作为注册中心 -->
   <dependency>
       <groupId>org.apache.curator</groupId>
       <artifactId>curator-framework</artifactId>
       <version>2.13.0</version>
   </dependency>
   <dependency>
       <groupId>org.apache.curator</groupId>
       <artifactId>curator-recipes</artifactId>
       <version>2.13.0</version>
   </dependency>
   ```

2. 引入application.yaml

   ```yaml
   # dubbo 配置项，对应 DubboConfigurationProperties 配置类
   dubbo:
     # Dubbo 应用配置
     application:
       name: user-service-provider
     # Dubbo 注册中心配置
     registry:
       address: zookeeper://127.0.0.1:2181
     # Dubbo 服务提供者协议配置
     protocol:
       port: -1
       name: dubbo
     # Dubbo 服务提供者配置
     provider:
       timeout: 1000
   ```

3. 使用@Service暴露服务

   ```java
   import org.apache.dubbo.config.annotation.Service;
   @Service
   public class UserServiceImpl implements UserService {
   
       @Override
       public User get(int id) {
           User user = new User();
           user.setId(id);
           user.setName("fox");
           user.setAge(30);
           return user;
       }
   
   }
   ```

   

4. 使用@EnableDubbo开启注解Dubbo功能 ，或者配置包扫描 

   dubbo.scan.base-packages=bat.ke.qq.com.service

   ```java
   @SpringBootApplication
   @EnableDubbo
   public class DubboUserServiceProviderApplication {
   
       public static void main(String[] args) {
          SpringApplication.run(DubboUserServiceProviderApplication.class,args);
   
       }
   }
   ```

**Consumer端**

1. 引入依赖

   ```xml
   <!-- 实现对 Dubbo 的自动化配置 -->
   <dependency>
       <groupId>org.apache.dubbo</groupId>
       <artifactId>dubbo</artifactId>
       <version>2.7.3</version>
   </dependency>
   <dependency>
       <groupId>org.apache.dubbo</groupId>
       <artifactId>dubbo-spring-boot-starter</artifactId>
       <version>2.7.3</version>
   </dependency>
   
   <!-- 使用 Zookeeper 作为注册中心 -->
   <dependency>
       <groupId>org.apache.curator</groupId>
       <artifactId>curator-framework</artifactId>
       <version>2.13.0</version>
   </dependency>
   <dependency>
       <groupId>org.apache.curator</groupId>
       <artifactId>curator-recipes</artifactId>
       <version>2.13.0</version>
   </dependency>
   ```

   

2. 配置 application.yaml

   ```yaml
   # dubbo 配置项，对应 DubboConfigurationProperties 配置类
   dubbo:
     # Dubbo 应用配置
     application:
       name: user-service-consumer
     # Dubbo 注册中心配置
     registry:
       address: zookeeper://127.0.0.1:2181
     # Dubbo 消费者配置
     consumer:
       timeout: 1000
   ```

   

3. 使用**@Reference**引用服务

   ```java
   @Reference
   private UserService userService;
   
   @Bean
   public ApplicationRunner runner() {
       return args -> {
           System.out.println(userService.get(1));
       };
   }
   ```

#### 5.6 整合actuator

通过引入spring-boot-starter-actuator，可以使用Spring Boot为我们提供的准生产环境下的应用监控和管理功能。我们可以通过HTTP，JMX，SSH协议来进行操作，自动得到审计、健康及指标信息等

引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

http 健康监控端点 默认只暴露了 health,info端点

- 通过management.endpoints.web.exposure.include=* 来指定开放所有的端点
- 通过 management.endpoints.web.exposure.include=health,info,beans  ，逗号分开来指定开放哪些端点
- 通过 management.endpoint.具体端点.enabled=true|false 来开放或者打开哪些端点

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

监控访问路径前缀

```markdown
management.endpoints.web.base-path=/actuator
```

actuator endpoints列表参考文档： https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-endpoints

具体端点分析：

- 服务监控端点 http://localhost:8080/actuator/health

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard-1592816835408.png)



- **服务装配bean信息端点** http://localhost:8080/actuator/beans

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard-1592816835409.png)

- 条件自动装配报告端点: http://localhost:8080/actuator/conditions

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard-1592816835409.png)

- 审计事件监控端点 http://localhost:8080/actuator/auditevents
- 配置属性(配置前缀)端点 http://localhost:8080/actuator/configprops
- **服务环境端点** http://localhost:8080/actuator/env

![img](SpringBoot自动配置实战及其原理剖析.assets/clipboard-1592816835409.png)

- **应用各级包中的日志等级级别端点** http://localhost:8080/actuator/loggers

![img](D:/我的文档/云笔记/chaosbead@163.com/b7918b03a9764b08ab642d22eee82d46/clipboard.png)

- **应用堆栈端点**  http://localhost:8080/actuator/heapdump   直接下载
- 线程dump端点监控 http://localhost:8080/actuator/threaddump

![img](D:/我的文档/云笔记/chaosbead@163.com/129bf2bf5cde4fc6aaa8c692b85f35e6/clipboard.png)

- 各项应用指标端点: http://localhost:8080/actuator/metrics

![img](D:/我的文档/云笔记/chaosbead@163.com/0244423657b44ebdaa002b50a614ce1c/clipboard.png)

- 定时任务端点 http://localhost:8080/actuator/scheduledtasks
- 应用映射端点 http://localhost:8080/actuator/mappings

![img](D:/我的文档/云笔记/chaosbead@163.com/69a85d31ca6e4a6c82f9257c012e0c76/clipboard.png)

- 最新调用监控端点: http://localhost:8080/actuator/httptrace
