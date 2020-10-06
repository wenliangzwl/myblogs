### spring-mybatis 的使用

  主程序
 
```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring-mybatis.xml"})
@Slf4j
public class MybatisTest {

  @Resource(name = "sqlSession")
  SqlSessionTemplate sqlSessionTemplate;

  @Test
  public void testUserMapper2() {
   User user=sqlSessionTemplate.selectOne("com.wlz.mapper.UserMapper.selectUser",1);
   User user2=sqlSessionTemplate.selectOne("com.wlz.mapper.UserMapper.selectUser",1);
   log.info("user:{}",user);
   log.info("user2:{}",user2);
  }

  @Autowired
  private UserMapper userMapper;

  @Test
  public void testUserMapper() {
    User selectUser = userMapper.selectUser(1);
    User selectUser2 = userMapper.selectUser(1);
    log.error("user:{}",selectUser);
    log.error("user2:{}",selectUser2);

  }
}
```
  spring-mybatis.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="mapperLocations" value="classpath*:mybatis/UserMappers.xml" />
    </bean>

    <bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource"
          init-method="init" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/mybatis?useUnicode=true" />
        <property name="username" value="root" />
        <property name="password" value="root" />
        <property name="initialSize" value="1" />
        <property name="minIdle" value="1" />
        <property name="maxActive" value="10" />
        <property name="maxWait" value="60000" />
        <property name="timeBetweenEvictionRunsMillis" value="50000" />
        <property name="minEvictableIdleTimeMillis" value="300000" />
        <property name="validationQuery" value="SELECT 'x'" />
        <property name="testWhileIdle" value="true" />
        <property name="testOnBorrow" value="false" />
        <property name="testOnReturn" value="false" />
        <property name="poolPreparedStatements" value="true" />
        <property name="maxPoolPreparedStatementPerConnectionSize"
                  value="20" />
        <property name="filters" value="stat" />
    </bean>

   <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="bat.ke.qq.mapper" />
    </bean>

    <bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
        <constructor-arg index="0" ref="sqlSessionFactory" />

    </bean>

    <bean id="userMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
        <property name="mapperInterface" value="com.wlz.mapper.UserMapper" />
        <property name="sqlSessionFactory" ref="sqlSessionFactory" />
    </bean>

  <!--  <bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
        <constructor-arg index="0" ref="sqlSessionFactory" />
    </bean>-->
</beans>
```
 mapper 
```
public interface UserMapper {

   //@Select("select * from user where id=#{id}")
   public User selectUser(Integer id);
}
```
 mapper.xml

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.wlz.mapper.UserMapper">
    <select id="selectUser" parameterType="integer" resultType="com.wlz.pojo.User">
        select * from user where id = #{id}
    </select>
</mapper>
```

### mybatis 的SqlSession  是线程不安全的而spring 整合 mybatis 的SqlSession 为什么是线程安全？
    
   底层是通过ThreadLocal 来保证线程安全的
   
   源码分析
   
   ![](mybatis.assets/sqlsessionTemplate.png)
   
   ![](mybatis.assets/sqlsessionTemplate2.png)
   
   ![](mybatis.assets/sqlsession线程安全.png)
   
   ![](mybatis.assets/sqlsession线程安全2.png)
   
   ![](mybatis.assets/sqlsession线程安全3.png)
   
   ![](mybatis.assets/sqlsession线程安全4.png)
   
   ![](mybatis.assets/sqlsession线程安全5.png)
   
   

### spring 整合 mybatis 的 mapper ioc 是怎么管理的 ？

   spring 管理 **mapper 是通过ioc 中的（MapperFactoryBean）去管理的
    
   通过 getObject() 获取 MapperFactoryBean 中的  **mapper（代理对象）
   
   ![](mybatis.assets/mybatis-spring存储mapper.png)