### `Spring Boot` 中使用 `MyBatis`

整合 `MyBatis` 之前，先搭建一个基本的 `Spring Boot` 项目，创建过程中引入 `mybatis-spring-boot-stater` 和数据库连接驱动（这里使用关系型数据库 `MySQL8.0`）

#### 1、mybatis-spring-boot-start

在 `pom.xml` 文件中引入：

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
	<version>2.1.2</version>
</dependency>
```

不同版本的 `Spring Boot` 和 `MyBatis` 版本对应不一样，具体可以查看[官方文档](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)。

通过 `IDEA` 右侧的 maven 视图我们可以查看到 `mybatis-spring-boot-starter` 都有哪些隐性依赖：

![](http://image.berlin4h.top/images/2020/06/20/image-20200620145805660.png)

可以看到其中包含了 `spring-boot-starter-jdbc`， 默认使用 `HikariCP` 作为数据源。

#### 2、`Druid` 数据源

`Druid` 是一个关系型数据库连接池，是阿里巴巴的一个[开源项目](https://github.com/alibaba/druid)。

`Druid` 不但提供连接池的功能，还提供监控功能，可以试试查看数据库连接池和`SQL` 查询的工作情况。

##### 配置 `Druid` 依赖

`Druid` 为 `Spring Boot` 项目提供了对应的 `starter` ：

```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>druid-spring-boot-starter</artifactId>
	<version>1.1.10</version>
</dependency>
```

##### `Druid` 数据源配置

上面看到 `MyBatis starter` 的依赖发现，`Spring Boot2` 以后默认使用 `HikariCP` 作为数据源，性能比 `Druid` 高，这里选择 `Durid` 是因为它功能最全面，具有 `sql` 拦截、统计数据等功能，且具有良好的扩展性，比较方便对 `jdbc` 接口进行监控跟踪等。为了使用 `Druid` 连接池，需要在 `application.yml` 下配置：

```yml
server:
  servlet:
    context-path: /web

spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      driver-class-name: com.mysql.jdbc.Driver
      url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&useSSL=false
      username: root
      password: root
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
      # 打开PSCache，并指定每个连接上PSCache的大小
      pool-prepared-statements: true
      max-open-prepared-statements: 20
      max-pool-prepared-statement-per-connection-size: 20
      # 配置监控统计拦截的filters, 去掉后监控界面sql无法统计，'wall'用于防火墙
      filters: stat,wall
      # Spring监控AOP切入点，如x.y.z.service.*，配置多个英文逗号分隔
      aop-patterns: com.springboot.service.*

      # WebStatFilter配置
      web-stat-filter:
        enabled: true
        # 添加过滤规则
        url-pattern: /*
        # 忽略过滤的格式
        exclusions: '*.js,*.git,*.jpg,*.png,*.css,*.ico,/druid/*'

      # StatViewServlet配置
      stat-view-servlet:
        enabled: true
        # 访问路径为/druid/时，跳转到StatViewServlet
        url-pattern: /druid/*
        # 是否能够重置数据
        reset-enable: false
        # 需要帐号密码才能访问控制台
        login-username: druid
        login-password: druid
        # IP白名单
        allow: 127.0.0.1
        # IP黑名单
        deny: 192.168.0.1

      # 配置StatFilter
      filter:
        stat:
          log-slow-sql: true
```

上述配置不但配置了 `Druid` 作为连接池，而且还开启了 `Druid` 监控功能。其他配置可参考[官方wiki](https://github.com/alibaba/druid/tree/master/druid-spring-boot-starter)。

此时，运行项目访问 `localhost:8080/web/druid`：

![](http://image.berlin4h.top/images/2020/06/20/20200620151846.png)

输入帐号密码即可进入 `Druid` 的监控后台：

![](http://image.berlin4h.top/images/2020/06/20/20200620164808.png)

关于 `Druid` 的更多说明，可以查看[官方wiki](https://github.com/alibaba/druid/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)

##### `Hikari`数据源配置参考，本文使用的是 `Druid` 数据源配置

```yml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: root
    hikari:
      pool-name: HikariCP #连接池名称
      auto-commit: true #此属性控制从池返回的连接的默认自动提交行为,默认值：true
      minimum-idle: 5 #最小空闲连接数量
      maximum-pool-size: 10 #连接池最大连接数，默认是10
      idle-timeout: 180000 #空闲连接存活最大时间，默认600000（10分钟）
      max-lifetime: 1800000 #此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟
      connection-timeout: 30000 #数据库连接超时时间,默认30秒，即30000
      connection-test-query: SELECT 1
      data-source-properties:
        cache-prep-stmts: true
        prep-stmt-cache-size: 250
        prep-stmt-cache-sql-limit: 2048
        use-server-prep-stmts: true
```



#### 3、使用 `MyBatis`

使用的库表：

```sql
-- auto Generated on 2020-06-05
DROP TABLE IF EXISTS student;
CREATE TABLE student(
    sno VARCHAR (50) NOT NULL COMMENT 'sno',
    `name` VARCHAR (50) NOT NULL COMMENT 'name',
    sex VARCHAR (50) NOT NULL COMMENT 'sex',
    PRIMARY KEY (sno)
    )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'student';

INSERT INTO STUDENT VALUES ('001', '小明', '男');
INSERT INTO STUDENT VALUES ('002', '小红', '女');
INSERT INTO STUDENT VALUES ('003', '小黑', '男');
```

创建对应的实体类：

```java
@Data
public class Student implements Serializable
{
    private static final long serialVersionUID = -5871112691729687726L;

    private String sno;

    private String name;

    private String sex;
}
```

创建一个包含基本`CRUD`的`Mapper`

- 使用注解的方式

```java
@Mapper
@Component
public interface StudentMapper
{
    @Insert("insert into student(sno,sname,ssex) values(#{sno}, #{name}, #{sex})")
    int add(Student student);

    @Update("update student set sname = #{name}, ssex = #{sex} where sno = #{sno}")
    int update(Student student);

    @Delete("delete from student where sno = #{sno}")
    int deleteBySno(String sno);

    @Select("select * from student where sno = #{sno}")
    @Results(id = "student", value = {
            @Result(property = "sno", column = "sno", javaType = String.class),
            @Result(property = "name", column = "name", javaType = String.class),
            @Result(property = "sex", column = "sex", javaType = String.class)
    })
    Student queryStudentBySno(String sno);
}
```

简单的语句只需要使用 `@Insert、@Update、@Delete、@Select` 这 4 个注解即可，动态 `SQL` 语句需要使用 `@InsertProvider、@UpdateProvider、@DeleteProvide、@SelectProviderr` 等注解。具体看参考 `MyBatis` [官方文档](http://www.mybatis.org/mybatis-3/zh/java-api.html)。

- 使用 `xml` 的方式

```java
public interface StudentMapper
{
    Student queryStudentBySno(@Param("sno") String sno);
}
```

`xml`文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.springboot.mapper.StudentMapper">
    <select id="queryStudentBySno" parameterType="string" resultType="com.springboot.bean.Student">
        select * from student where sno = #{sno}
    </select>
</mapper>
```

注意要添加 `@MapperScan("com.springboot.mapper")` 注解，且需要在 `application.yml` 中添加一些额外的配置：

```yml
mybatis:
  # type-aliases 扫描路径
  # type-aliases-package:
  # mapper xml 实现扫描路径
  mapper-locations: classpath:mapper/*.xml
```

#### 4、测试

接下来编写 `Service` ：

```java
public interface StudentService
{
    int add(Student student);
    
    int update(Student student);
    
    int deleteByIds(String sno);

    Student queryStudentBySno(String sno);
}
```

实现类：

```java
@Service("studentService")
public class StudentServiceImpl implements StudentService
{
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public int add(Student student)
    {
        return this.studentMapper.add(student);
    }

    @Override
    public int update(Student student)
    {
        return this.studentMapper.update(student);
    }

    @Override
    public int deleteByIds(String sno)
    {
        return this.studentMapper.deleteBySno(sno);
    }

    @Override
    public Student queryStudentBySno(String sno)
    {
        return this.studentMapper.queryStudentBySno(sno);
    }
}
```

编写 `Controller`：

```java
@RestController
public class TestController
{
    @Autowired
    private StudentService studentService;

    @GetMapping("/query-student")
    public Student queryStudentBySno(String sno)
    {
        return this.studentService.queryStudentBySno(sno);
    }
}
```

完整的项目目录如下图所示：

![](http://image.berlin4h.top/images/2020/06/20/20200620171038.png)

项目启动后访问：`localhost:8080/web/query-student?sno=001`

![](http://image.berlin4h.top/images/2020/06/20/20200620171147.png)

查看 `SQL` 监控情况：

![](http://image.berlin4h.top/images/2020/06/20/20200620171217.png)

可以看到我们访问 `/query-student` 的记录。

