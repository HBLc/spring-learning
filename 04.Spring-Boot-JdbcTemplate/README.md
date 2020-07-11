### Spring Boot 中使用 JdbcTemplate

JdbcTemplate 相较于 MyBatis、Hibernate 等数据库框架更容易上手，对 SQL 的操作也更为直观方便，所以在项目中也是一个不错的选择。在 Spring Boot 开启 JdbcTemplate 很简单，只需要引入 `spring-boot-starter-jdbc` 依赖即可。JdbcTemplate 封装了许多 SQL 操作，具体可以查阅[官方文档](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/JdbcTemplate.html)。

**引入依赖**

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

数据库驱动为 `mysql-connector-java`，数据源采用 Druid。

```xml
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>5.1.46</version>
</dependency>
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>druid-spring-boot-starter</artifactId>
	<version>1.1.10</version>
</dependency>
```

**代码编写**

数据准备：

```sql
DROP TABLE IF EXISTS student;
CREATE TABLE student
(
    sno VARCHAR(50) NOT NULL COMMENT 'sno', 
    `name` VARCHAR(50) NOT NULL COMMENT 'name',
    sex VARCHAR(50) NOT NULL COMMENT 'sex',
    PRIMARY KEY (sno)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'student';

INSERT INTO STUDENT VALUES ('001', '小明', '男');
INSERT INTO STUDENT VALUES ('002', '小红', '女');
INSERT INTO STUDENT VALUES ('003', '小黑', '男');
```

这里主要演示在 Dao 的实现类里使用 JdbcTemplate，所以其他模块的代码就不展示了，具体可参考源码。

StudentDaoImpl 类代码：

```java
@Repository("studentDao")
public class StudentDaoImpl implements StudentDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public int add(Student student) {
        // String sql = "insert into student(sno, name, sex) values(?,?,?)";
        // Object[] args = { student.getSno(), student.getName(), student.getSex() };
        // int[] argTypes = { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR };
        // return this.jdbcTemplate.update(sql, args, argTypes);
        String sql = "insert into student(sno, name, sex) values(:sno, :name, :sex)";
        NamedParameterJdbcTemplate npjt = new NamedParameterJdbcTemplate(this.jdbcTemplate.getDataSource());
        return npjt.update(sql, new BeanPropertySqlParameterSource(student));
    }
    
    @Override
    public int update(Student student) {
        String sql = "update student set name = ?, sex = ? where sno = ?";
        Object[] args = { student.getName(). student.getSex(), student.getSno() };
        int[] argTypes = { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR };
        return this.jdbcTemplate.update(sql, args, argTypes);
    }
    
    @Override
    public int deleteBySno(String sno) {
        String sql = "delete from student where sno = ?";
        Object[] args = { sno };
        int[] argTypes = { Types.VARCHAR };
        return this.jdbcTemplate.update(sql, args, argTypes);
    }
    
    @Override
    public Student queryStudentBySno(String sno) {
        String sql = "select * from student where sno = ?";
        Object[] args = { sno };
        int[] argTypes = { Types.VARCHAR };
        List<Student> studentList = this.jdbcTemplate.query(sql, args, argTypes, new StudentMapper());
        if (!studentList.isEmpty()) {
            return studentList.get(0);
        }
        return null;
    }
}
```

在引入 `spring-boot-starter-jdbc` 驱动后，可直接在类中注入 JdbcTemplate。由上面的代码可发现，对于保存操作有两种不同的方法，当插入的表字段较多的情况下，推荐使用 `NamedParameterJdbcTemplate`。

对于返回结果，可以直接使用 `List<Map<String, Object>>` 来接收，这也是个人比较推荐使用的方式，毕竟比较简单方便；也可以使用库表对应的实体对象来接收，不过这时我们就需要手动创建一个实现了 `org.springframework.jdbc.core.RowMapper` 的对象，用于将实体对象属性和库表字段一一对应：

```java
public class StudentMapper implements RowMapper<Student> {
    @Override
    public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
        Student student = new Student();
        student.setSno(rs.getString("sno"));
        student.setName(rs.getString("name"));
        student.setSex(rs.getString("sex"));
        return student;
    }
}
```

**测试**

启动项目，测试插入数据 `http://localhost:8080/web/api/v1/student/add?sno=004&name=小灰&sex=男`

![](http://image.berlin4h.top/images/2020/07/11/20200711155236.png)

查询所有的学生数据 `http://localhost:8080/web/api/v1/student`

![](http://image.berlin4h.top/images/2020/07/11/20200711155356.png)

查询指定学生数据 `http://localhost:8080/web/api/v1/student/query?sno=004`

![](http://image.berlin4h.top/images/2020/07/11/20200711155451.png)

测试删除 `http://localhost:8080/web/api/v1/studnet/delete?sno=004`

![](http://image.berlin4h.top/images/2020/07/11/20200711155803.png)

