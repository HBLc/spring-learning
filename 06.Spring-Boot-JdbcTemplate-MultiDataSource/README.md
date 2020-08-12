### Spring Boot JdbcTemplate 配置 Druid 多数据源

JdbcTemplate 配置 Druid 多数据源的核心在于创建 JdbcTemplate 的时候为其分配不同的数据源，然后在需要访问不同数据库的时候使用对应的 JdbcTemplate 即可。这里介绍在 Spring Boot 中基于 Oracle 和 MySQL 配置 Druid 多数据源。

**引入依赖**

先使用 IDEA 开启一个最简单的 Spring Boot 应用，然后引入如下配置：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<!-- Oracle 驱动 -->
<dependency>
    <groupId>com.oracle</groupId>
    <artifactId>ojdbc6</artifactId>
    <version>6.0</version>
</dependency>
<!-- MySQL 驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<!-- Druid 数据源 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.1.22</version>
</dependency>
```

**多数据源配置**

接着在 Spring Boot 配置文件 application.yml 中配置多数据源：

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

然后创建一个多数据源配置类，根据 application.yml 分别配置一个 MySQL 和 Oracle 的数据源，并将这个两个数据源注入到不同的 JdbcTemplate 中：

```java
@Configuration
public class DataSourceConfig
{
    @Primary
    @Bean(name = "mySqlDataSource")
    @ConfigurationProperties("spring.datasource.druid.mysql")
    public DataSource dataSourceOne()
    {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "oracleDataSource")
    @ConfigurationProperties("spring.datasource.druid.oracle")
    public DataSource dataSourceTwo()
    {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean("mySqlJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("mySqlDataSource") DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }

    @Bean("oracleJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate(@Qualifier("oracleDataSource") DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }
}
```

上述代码根据 application.yml 创建了 mySqlDataSource 和 oracleDataSource 数据源，其中 mySqlDataSource 用 @Primary 标注为主数据源，接着根据这两个数据源创建了 mySqlJdbcTemplate 和 oracleJdbcTemplate。

`@Primary` 标志这个 Bean 如果在多个同类 Bean 候选时，该 Bean 优先被考虑。多数据源配置的时候，必须要有一个主数据源，用 `@Primary` 标志该 Bean。

数据源创建完毕，接下来开始进行测试代码编写。

**测试**

首先往 MySQL 和 Oracle 中创建测试表，并插入一些数据：

MySQL：

```sql
DROP TABLE IF EXISTS student;
CREATE TABLE student
(
    sno VARCHAR(50) NOT NULL COMMENT 'sno',
    `name` VARCHAR(50) NOT NULL COMMENT 'name',
    sex VARCHAR(50) NOT NULL COMMENT 'sex',
    `DATASOURCE` varchar(50) DEFAULT NULL,
    PRIMARY KEY (sno)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'student';

INSERT INTO STUDENT VALUES ('001', '小明', '男', 'mysql');
INSERT INTO STUDENT VALUES ('002', '小红', '女', 'mysql');
INSERT INTO STUDENT VALUES ('003', '小黑', '男', 'mysql');
```

Oracle：

```sql
DROP TABLE STUDENT;
CREATE TABLE STUDENT (
     SNO VARCHAR2(3 BYTE) NOT NULL ,
     NAME VARCHAR2(9 BYTE) NOT NULL ,
     SEX CHAR(2 BYTE) NOT NULL ,
     DATASOURCE VARCHAR2(10 BYTE) NULL
)
LOGGING NOCOMPRESS NOCACHE;

-- ----------------------------
-- Records of STUDENT
-- ----------------------------
INSERT INTO STUDENT VALUES ('001', 'KangKang', 'M ', 'oracle');
INSERT INTO STUDENT VALUES ('002', 'Mike', 'M ', 'oracle');
INSERT INTO STUDENT VALUES ('003', 'Jane', 'F ', 'oracle');
INSERT INTO STUDENT VALUES ('004', 'Maria', 'F ', 'oracle');

-- ----------------------------
-- Checks structure for table STUDENT
-- ----------------------------
ALTER TABLE STUDENT ADD CHECK (SNO IS NOT NULL);
ALTER TABLE STUDENT ADD CHECK (NAME IS NOT NULL);
ALTER TABLE STUDENT ADD CHECK (SEX IS NOT NULL);
```

接着创建两个 Dao 及其实现类，分别用于 MySQL 和 Oracle 中获取数据：

MySqlStudentDao 接口：

```java
public interface MySqlStudentDao()
{
    List<Map<String, Object>> getAllStudents();
}
```

MySqlStudentDao 实现：

```java
@Repository
public class MySqlStudentDaoImpl implements MySqlStudentDao
{
    private final JdbcTemplate jdbcTemplate;

    public MySqlStudentDaoImpl(@Qualifier("mySqlJdbcTemplate") JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> getAllStudents()
    {
        return jdbcTemplate.queryForList("select * from student");
    }
}
```

可看到，在 MySqlStudentDaoImpl 中注入的是 mySqlJdbcTemplate

OracleStudentDao 接口：

```java
public interface OracleStudentDao
{
    List<Map<String, Object>> getAllStudents();
}
```

OracleStudentDao 实现：

```java
@Repository
public class OracleStudentDaoImpl implements OracleStudentDao
{
    private final JdbcTemplate jdbcTemplate;

    public OracleStudentDaoImpl(@Qualifier("oracleJdbcTemplate") JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> getAllStudents()
    {
        return jdbcTemplate.queryForList("select * from student");
    }
}
```

在 OracleStudentDaoImpl 中注入的是 oracleJdbcTemplate。

随后编写 Service 层：

StudentService：

```java
public interface StudentService
{
    List<Map<String, Object>> getAllStudentsFromOracle();
    List<Map<String, Object>> getAllStudentsFromMySQL();
}
```

StudentService 实现：

```java
@Service("studentService")
public class StudentServiceImpl implements StudentService
{
    private final MySqlStudentDao mySqlStudentDao;
    private final OracleStudentDao oracleStudentDao;

    public StudentServiceImpl(MySqlStudentDao mySqlStudentDao, OracleStudentDao oracleStudentDao)
    {
        this.mySqlStudentDao = mySqlStudentDao;
        this.oracleStudentDao = oracleStudentDao;
    }

    @Override
    public List<Map<String, Object>> getAllStudentsFromMySQL()
    {
        return mySqlStudentDao.getAllStudents();
    }

    @Override
    public List<Map<String, Object>> getAllStudentsFromOracle()
    {
        return oracleStudentDao.getAllStudents();
    }
}
```

最后编写一个 Controller：

```java
@RestController
@RequestMapping("/api/v1/student")
public class StudentController
{
    private final StudentService studentService;

    public StudentController(StudentService studentService)
    {
        this.studentService = studentService;
    }

    @GetMapping("/mysql")
    public List<Map<String, Object>> queryStudentsFromMySQL()
    {
        return studentService.getAllStudentsFromMySQL();
    }

    @GetMapping("/oracle")
    public List<Map<String, Object>> queryStudentsFromOracle()
    {
        return studentService.getAllStudentsFromOracle();
    }
}
```

最终项目结构如下图所示：

![](http://image.berlin4h.top/images/2020/08/12/20200812182016.png)

启动项目访问：http://localhost:8877/web/api/v1/student/mysql

![](http://image.berlin4h.top/images/2020/08/12/20200812182102.png)

访问：http://localhost:8877/web/api/v1/student/oracle

![](http://image.berlin4h.top/images/2020/08/12/20200812182120.png)

