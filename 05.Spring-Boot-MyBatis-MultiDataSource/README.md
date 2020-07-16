### Spring Boot MyBatis 配置 Druid 多数据源

回顾在 Spring 中配置 MyBatis SqlSessionFactory 的配置：

```xml
<!-- MyBatis 的 SqlSessionFactory -->
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean" scope="prototype">
    <property name="dataSource" ref="dataSource"/>
	<property name="configLocation" value="classpath:mybatis-config.xml"/>
</bean>
```

所以实际上在 Spring Boot 中配置 MyBatis 多数据源的关键在于创建 SqlSessionFactory 的时候为其分配不同的数据源。

**引入依赖**

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.1.2</version>
</dependency>

<!-- MySQL驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.46</version>
</dependency>

<!-- Oracle驱动 -->
<dependency>
    <groupId>com.oracle</groupId>
    <artifactId>ojdbc6</artifactId>
    <version>6.0</version>
</dependency>

<!-- Druid数据源 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.1.10</version>
</dependency>
```

**多数据源的配置**

在 Spring Boot 配置文件 applization.yml 中配置多数据源：

```yml
server:
  servlet:
    context-path: /web


spring:
  datasource:
    druid:
      # 数据库访问配置, 使用druid数据源
      # 数据源1 mysql
      mysql:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&autoReconnect=true&failOverReadOnly=false&zeroDateTimeBehavior=convertToNull
        username: root
        password: root
      # 数据源2 oracle
      oracle:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: oracle.jdbc.driver.OracleDriver
        url: jdbc:oracle:thin:@192.168.0.201:1521:bcdev
        username: WORKFLOW
        password: WORKFLOW

      # 连接池配置
      initial-size: 5
      min-idle: 5
      max-active: 20
      # 连接等待超时时间
      max-wait: 30000
      # 配置检测可以关闭的空闲连接间隔时间
      time-between-eviction-runs-millis: 60000
      # 配置连接在池中的最小生存时间
      min-evictable-idle-time-millis: 300000
      validation-query: select '1' from dual
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      # 打开PSCache，并且指定每个连接上PSCache的大小
      pool-prepared-statements: true
      max-open-prepared-statements: 20
      max-pool-prepared-statement-per-connection-size: 20
      # 配置监控统计拦截的filters, 去掉后监控界面sql无法统计, 'wall'用于防火墙
      filters: stat,wall
      # Spring监控AOP切入点，如x.y.z.service.*,配置多个英文逗号分隔
      aop-patterns: com.springboot.servie.*


      # WebStatFilter配置
      web-stat-filter:
        enabled: true
        # 添加过滤规则
        url-pattern: /*
        # 忽略过滤的格式
        exclusions: '*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*'

      # StatViewServlet配置
      stat-view-servlet:
        enabled: true
        # 访问路径为/druid时，跳转到StatViewServlet
        url-pattern: /druid/*
        # 是否能够重置数据
        reset-enable: false
        # 需要账号密码才能访问控制台
        login-username: druid
        login-password: druid123
        # IP白名单
        # allow: 127.0.0.1
        #　IP黑名单（共同存在时，deny优先于allow）
        # deny: 192.168.1.218

      # 配置StatFilter
      filter:
        stat:
          log-slow-sql: true
```

然后根据 application.yml 创建两个数据源配置类 MysqlDatasourceConfig 和 OracleDatasourceConfig：

MysqlDatasourceConfig：

```java
@Configuration
@MapperScan(basePackages = MySqlDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "mySqlSqlSessionFactory")
public class MySqlDataSourceConfig {
    // mysqldao 扫描路径
    static final String PACKAGE = "com.springboot.mysqldao";

    // mybatis mapper 扫描路径
    static final String MAPPER_LOCATION = "classpath:mapper/mysql/*.xml";

    @Primary
    @Bean(name = "mySqlDataSource")
    @ConfigurationProperties("spring.datasource.druid.mysql")
    public DataSource mySqlDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "mySqlTransactionManager")
    public DataSourceTransactionManager mySqlTransactionManager() {
        return new DataSourceTransactionManager(mySqlDataSource());
    }

    @Primary
    @Bean(name = "mySqlSqlSessionFactory")
    public SqlSessionFactory mySqlSqlSessionFactory(@Qualifier("mySqlDataSource") DataSource dataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        // 如果不使用 xml 的方式配置 mapper, 则可以省去下面这行 mapper location 的配置
        sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MySqlDataSourceConfig.MAPPER_LOCATION));
        return sessionFactoryBean.getObject();
    }
}
```

上面代码配置了一个名为 mySqlDataSource 的数据源，对应 application.yml 中 spring.datasource.druid.mysql 前缀配置的数据库。然后创建了一个名为 mySqlSessionFactory 的 Bean，并且注入了 mySqlDataSource。与此同时，还分别定了两个扫描路径 PACKAGE 和 MAPPER_LOCATION，前者为 Mysql 数据库对应的 Mapper 接口地址，后者为对应的 mapper.xml 文件路径。

`@Primary` 标志这个 Bean 如果在多个同类 Bean 候选时，该 Bean 优先被考虑。多数据源配置的时候，必须要有一个主数据源，用 `@Primary` 标志该 Bean。

同理，接着配置 Oracle 数据库对应的配置类：

OracleDataSourceConfig：

```java
@Configuration
@MapperScan(basePackages = OracleDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "oracleSqlSessionFactory")
public class OracleDataSourceConfig
{
    // oracledao 扫描路径
    static final String PACKAGE = "com.springboot.oracledao";

    // mybatis mapper 扫描路径
    static final String MAPPER_LOCATION = "classpath:mapper/oracle/*.xml";

    @Bean(name = "oracleDataSource")
    @ConfigurationProperties("spring.datasource.druid.oracle")
    public DataSource oracleDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "oracleTransactionManager")
    public DataSourceTransactionManager oracleTransactionManager() {
        return new DataSourceTransactionManager(oracleDataSource());
    }

    @Bean(name = "oracleSqlSessionFactory")
    public SqlSessionFactory oracleSqlSessionFactory(@Qualifier("oracleDataSource") DataSource dataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        // 如果不使用 xml 的方式配置 mapper, 则可以省去下面这行 mapper location 的配置
        sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(OracleDataSourceConfig.MAPPER_LOCATION));
        return sessionFactoryBean.getObject();
    }
}
```

**测试**

配置完多数据源，接下来分别在 com.springboot.mysqldao 路径和 com.springboot.oracledao 路径下创建两个 mapper 接口：

MySqlStudentMapper：

```java
@Mapper
public interface MySqlStudentMapper {
    List<Map<String, Object>> getAllStudents();
}
```

OracleStudnetMapper：

```java
@Mapper
public interface OracleStudentMapper {
    List<Map<String, Object>> getAllStudnets();
}
```

接着创建 mapper 接口对应的实现：

在 src/main/resource/mapper/mysql/ 路径下创建 MySqlStudentMapper.xml：

```xml
<?xml version="1.0" encoding="UTF-8" ?>    
    <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"   
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">     
<mapper namespace="com.springboot.mysqldao.MySqlStudentMapper">
    <select id="getAllStudents" resultType="java.util.Map">
        select * from student
    </select>
</mapper>
```

在 src/main/resource/mapper/oracle/ 路径下创建 OracleStudentMapper.xml：

```xml
<?xml version="1.0" encoding="UTF-8" ?>    
    <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"   
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.springboot.oracledao.OracleStudentMapper">
    <select id="getAllStudents" resultType="java.util.Map">
        select * from student
    </select>
</mapper>
```

StudentService：

```java
@Service("studentService")
public class StudentServiceImpl implements StudentService {
    @Autowired
    private OracleStudentMapper oracleStudentMapper;
    
    @Autowired
    private MySqlStudentMapper mySqlStudentMapper;
    
    List<Map<String, Object>> getAllStudentsFromOracle() {
        return this.oracleStudentMapper.getAllStudents();
    }
    List<Map<String, Object>> getAllStudentsFromMySql() {
        return this.mysqlStudentMapper.getAllStudents();
    }
}
```

StudentController：

```java
@RestController
public class StudentController {
    @Autowired
    private StudentService studentService;
    
    @RequestMapping("query-student-oracle")
    public List<Map<String, Oracle>> queryStudentsFromOracle() {
        return this.studentService.getAllStudentsFromOracle();
    }
    
    @RequestMapping("query-student-mysql")
    public List<Map<String, Oracle>> queryStudentsFromMySql() {
        return this.studentService.getAllStudentsFromMySql();
    }
}
```

最终项目目录结构如图所示：

![](http://image.berlin4h.top/images/2020/07/16/20200716160601.png)

启动项目访问：http://localhost:8080/web/query-student-mysql：

![](http://image.berlin4h.top/images/2020/07/16/20200716160712.png)

http://localhost:8080/web/query-student-oracle：

![](http://image.berlin4h.top/images/2020/07/16/20200716160725.png)
