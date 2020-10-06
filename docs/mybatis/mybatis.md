### 传统 JDBC 的弊端
    
   1、jdbc 底层没有用连接池、操作数据库需要频繁的创建和关联链接。消耗很大的资源
   
   2、写原生的 jdbc 代码在 java 中，一旦我们要修改 sql 的话，java 需要整体编译，不利于系统维护
   
   3、使用 PreparedStatement 预编译的话对变量进行设置 123 数字，这样的序号不利于维护
   
   4、返回 result 结果集也需要硬编码。

####  Java 的jdbc 连接数据库 

 非对象连接数据库
```
public class Jdbc01 {

  public static void main(String[] args) {

    insert("wlz",18);
  }

  static void insert(String name,int age)
  {
    String sql="insert into user(username,age) value(?,?)";
    // 连接数据库
    Connection conn= DbUtil.open();
    try {

      PreparedStatement pstmt=(PreparedStatement) conn.prepareStatement(sql);
      pstmt.setString(1,name);
      pstmt.setInt(2,age);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    finally {
      DbUtil.close(conn);
    }

  }
}

```
 面向对象 连接数据库
```
public class Jdbc02 {

    public static void main(String[] args) {
      User c=new User();
      c.setUsername("wlz");
      c.setAge(18);
      c.setId(2);
      insert(c);
      //查询
       c=query(1);
      log.info("userid:{},name:{}",c.getId(),c.getUsername());
    }

    static void insert(User c) {
      String sql="insert into user(username,age) value(?,?)";
      Connection conn= DbUtil.open();
      try {
        PreparedStatement pstmt=(PreparedStatement) conn.prepareStatement(sql);
        pstmt.setString(1,c.getUsername());
        pstmt.setInt(2,c.getAge());
        pstmt.executeUpdate();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      finally {
        DbUtil.close(conn);
      }
  }

  static User query(int id) {
    String sql="select * from user where id=?";
    Connection conn= DbUtil.open();
    try {
      PreparedStatement pstmt=(PreparedStatement) conn.prepareStatement(sql);
      pstmt.setInt(1,id);
      ResultSet rs=pstmt.executeQuery();
      if(rs.next())
      {
        Integer ids = rs.getInt(1);
        String name=rs.getString(2);
        User c=new User();
        c.setId(ids);
        c.setUsername(name);
        return c;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    finally {
      DbUtil.close(conn);
    }
    return null;
  }
}
```

 数据库连接工具类
```
public class DbUtil {

    /*
     * 打开数据库
     */
    private static String driver;//连接数据库的驱动
    private static String url;
    private static String username;
    private static String password;

    static {
      driver="com.mysql.cj.jdbc.Driver";//需要的数据库驱动
      url="jdbc:mysql://localhost:3306/mybatis?serverTimezone=UTC";//数据库名路径
      username="root";
      password="root";
    }
    public static Connection open() {
      try {
        /**   将mysql驱动注册到DriverManager中去(加载数据库驱动)
         * 1.Class.forName() 方法要求JVM查找并加载指定的类到内存中；
         * 2.将"com.mysql.jdbc.Driver" 当做参数传入，就是告诉JVM，去"com.mysql.jdbc"这个路径下找Driver类，将其加载到内存中。
         * 3.由于JVM加载类文件时会执行其中的静态代码块，从Driver类的源码中可以看到该静态代码块执行的操作是：将mysql的driver注册到系统的DriverManager中。
         */
        Class.forName(driver);

        /**
         * DriverManager.getConnection方法分析
         *    1.getConnection
         *    2.调用自身的 getConnection
         *    3.对特殊的ArrayList 进行遍历 调用 connect(url, info); 方法 > 不同数据库实现的接口
         *    4. 获取connection 对象
         */
        return (Connection) DriverManager.getConnection(url,username, password);
      } catch (Exception e) {
        System.out.println("数据库连接失败！");
        e.printStackTrace();
      }//加载驱动

      return null;
    }

    /*
     * 关闭数据库
     */
    public static void close(Connection conn) {
      if(conn!=null)
      {
        try {
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }
```
 


### ORM框架Mybatis介绍
 
   MyBatis is a first class persistence framework with support for custom SQL, stored procedures and advanced mappings. MyBatis eliminates almost all of the JDBC
    code and manual setting of parameters and retrieval of results. MyBatis can use simple XML or Annotations for configuration and map primitives, 
    Map interfaces and Java POJOs (Plain Old Java Objects) to database records.
    
   MyBatis 是一款优秀的持久层框架，它支持定制化 SQL、存储过程以及高级映射。MyBatis 避免了几乎所有的 JDBC 代码和手动设置参数以及获取结果集。
   MyBatis 可以使用简单的 XML 或注解来配置和映射原生类型、接口和 Java 的 POJO（Plain Old Java Objects，普通老式 Java 对象）为数据库中的记录。

### Mybatis快速开始

  主程序入口
```
public class MybatisMain {
  public static void main(String[] args) throws IOException {
    String resource = "mybatis-config.xml";//全局配置
    InputStream inputStream = Resources.getResourceAsStream(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    SqlSession session = sqlSessionFactory.openSession();
    Blog blog = session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 1);
    System.out.println(blog);
  }
}
```

  主配置文件
  
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  // 加载配置文件
  <properties resource="jdbc.properties"/>

  <environments default="development">
    <environment id="development">
      <transactionManager type="JDBC"/>
      <dataSource type="POOLED">
        <property name="driver" value="${driver}"/>
        <property name="url" value="${url}"/>
        <property name="username" value="${username}"/>
        <property name="password" value="${password}"/>
      </dataSource>
    </environment>
  </environments>

  <mappers>
      <mapper resource="mapper/BlogMapper.xml"/>
  </mappers>
</configuration>

```
 mapper.xml 
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.mybatis.example.BlogMapper">

  <select  id="selectBlog"   resultType="com.wlz.pojo.Blog">
    select * from blog where id = #{id}
  </select>
</mapper>
```

  jdbc.properties
```
#driver=com.mysql.jdbc.Driver
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://localhost:3306/mybatis?serverTimezone=UTC
username=root
password=root

```
