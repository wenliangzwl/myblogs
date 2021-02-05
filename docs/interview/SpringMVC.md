### SpringMVC 面试题

#### 1. 什么是SpringMVC 
   
   Spring MVC 是一个基于Java 的实现MVC 设计模式的请求驱动类型的轻量级Web框架，通过把模型-视图-控制器分离，将Web 层进行职责解耦。
   把复杂的web 应用分成逻辑清晰的几部分，简化开发，减少出错，方便组内开发人员之间的配合。
   
#### 2. Spring MVC 的主要组件
   
   1.前端控制器DispatcherServlet (不需要程序员自己开发)
       
       作用: 接收请求，响应结果，相当于转发器，有了DispatcherServlet 就减少了其他组件之间的耦合度。
   
   2.处理器映射器HandlerMapping (不需要程序员开发)
        
        作用: 根据请求的URL 来查找Handler 
   
   3. 处理器适配器HandlerAdapter 
        
        注意: 在编写Handler 的时候要按照HandlerAdapter 要求的规则去编写，这样适配器HandlerAdapter 才可以正确的去执行Handler. 
        
   4. 处理器Handler (需要程序员开发)
   
   5. 视图解析器 ViewResolver (不需要程序员开发)
       
       作用: 进行视图的解析，根据视图逻辑名解析成真正的视图(view)
       
   6. 视图view (需要程序员开发jsp)
   
      View 是一个接口,它的实现类支持不同的视图类型(jsp,freemarker , pdf 等等) 
      
#### 3. Spring MVC 的控制器是不是单例模式，如果是，有什么问题，怎么解决?
   
   是单例模式, 所以在多线程访问的时候有线程安全问题，不要用同步，会影响性能的，解决方案是在控制器里面不能写字段。
   
#### 4. 注解原理是什么
   
   注解本质是一个继承了Annotation 的特殊接口，其具体实现类是Java 运行时生成的动态代理类.我们通过反射获取注解时,返回的是Java
   运行时生成的动态代理对象。通过代理对象调用自定义注解的方式，会最终调用AnnotationInvocationHandler 的invoke 方法。
   该方法会从memberValues 这个Map 中索引出对应的值。而memberValues 的来源是java 常量池。
   
#### 5. @PathVariable 和 @RequestParam 的区别
   
   请求路径上有个id 的变量值，可以通过@PathVariable 来获取 
   
   @RequestMapping(value = "/page/{id}",method=RequestMethod.GET) 
   
   @RequestParam 用来获取静态的URL 请求入参 

#### 6. 介绍一下 WebApplicationContext 
   
   WebApplicationContext 继承了ApplicationContext 并增加了一些WEB 应用必备的特有功能,它不同于一般的ApplicationContext ,
   因为它能处理主题, 并找到被关联的servlet.

#### 7. Spring MVC 与 struts2 区别
   
   相同点: 都是基于mvc 的表现层框架, 都用于web 项目的开发
   
   不同点: 
       
       1. 前端控制器不一样。spring mvc 的前端控制器是servlet ：DispatcherServlet .
           struts2 的前端控制器是 filter ：strutsPreparedAndExcutorFilter。
       
       2. 请求参数的接收方式不一样。spring mvc 是使用方法的形参接收请求的参数，基于方法开发，线程安全，可以设计为单例或者多例开发.
       struts2 是通过类的成员变量接收请求的 参数，是基于类的开发，线程不安全，只能设计未多例的开发.
       
       3.struts 采用值栈存储请求和响应数据，通过OGNL存取数据，spring mvc 通过参数解析器是将request 请求内容解析，并给方法形参赋值，
       将数据和视图封装成ModelAndView 对象，最后又将ModelAndView 中的模型数据通过request 域传输到页面。 jsp 视图解析器默认使用 jstl.
       
       4. 与spring 整合。spring mvc 是spring 框架的一部分，不需要整合。

