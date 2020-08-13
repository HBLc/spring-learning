### Spring Boot AOP 记录用户操作日志

在 Spring 架构中，使用 AOP 配置自定义注解可以方便的实现用户操作的监控。首先搭建一个基本的 Spring Boot Web 项目，然后引入必要的依赖：

```xml
<!-- Druid 数据源驱动 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.1.22</version>
</dependency>
<!-- Oracle 驱动 -->
<dependency>
    <groupId>com.oracle</groupId>
    <artifactId>ojdbc6</artifactId>
    <version>6.0</version>
</dependency>
<!-- AOP 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**自定义注解**

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log
{
    String value() default "";
}
```

**创建库表和实体**

在数据库中创建一张 `sys_log` 表，用于保存用户的操作日志，数据库采用 `Oracle 11G`：

```sql
CREATE TABLE SYS_LOG (
    ID NUMBER(20) NOT NULL,
    USERNAME VARCHAR2(50 BYTE) NULL,
    OPERATION VARCHAR2(50 BYTE) NULL,
    TIME NUMBER(11) NULL,
    METHOD VARCHAR2(200 BYTE) NULL,
    PARAMS VARCHAR2(500 BYTE) NULL,
    IP VARCHAR2(64 BYTE) NULL,
    CREATE_TIME DATE NULL
);

COMMENT ON COLUMN SYS_LOG.USERNAME IS '用户名';
COMMENT ON COLUMN SYS_LOG.OPERATION IS '用户操作';
COMMENT ON COLUMN SYS_LOG.TIME IS '响应时间';
COMMENT ON COLUMN SYS_LOG.METHOD IS '请求方法';
COMMENT ON COLUMN SYS_LOG.PARAMS IS '请求参数';
COMMENT ON COLUMN SYS_LOG.CREATE_TIME IS '创建时间';

-- 创建一个序列，可用于生成在多个表间唯一的主键值，也可用于生成表的缺省值
CREATE SEQUENCE seq_sys_log START WITH 1 INCREMENT BY 1;
```

库表对应的实体：

```java
@Data
public class SysLog implements Serializable
{
    private static final long serialVersionUID = -6309732882044872298L;
    private Integer id;
    private String username;
    private String operation;
    private Integer time;
    private String method;
    private String params;
    private String ip;
    private Date createTime;
}
```

**保存日志的方法**

为了方便，这里直接使用 Spring JdbcTemplate 来操作数据库。定义一个 SysLogDao 接口，包含一个保存操作日志的抽象方法：

```java
public interface SysLogDao
{
    void saveSysLog(SysLog sysLog);
}
```

其实现方法：

```java
@Repository
public class SysLogDaoImpl implements SysLogDao
{
    private final JdbcTemplate jdbcTemplate;

    public SysLogDaoImpl(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveSysLog(SysLog sysLog)
    {
        StringBuffer sql = new StringBuffer(" insert into sys_log ");
        sql.append(" (id,username,operation,time,method,params,ip,create_time) ");
        sql.append(" values(seq_sys_log.nextval,:username,:operation,:time,:method, ");
        sql.append(" :params,:ip,:createTime) ");

        NamedParameterJdbcTemplate npjt = new NamedParameterJdbcTemplate(this.jdbcTemplate.getDataSource());
        npjt.update(sql.toString(), new BeanPropertySqlParameterSource(sysLog));
    }
}
```

**切面和切点**

定义一个 LogAspect 类，使用 `@Aspect` 标注让其成为一个切面，切点为使用 `@Log` 注解标注的方法，使用 `@Around` 环绕通知：

```java
@Aspect
@Component
public class LogAspect
{
    private final SysLogDao sysLogDao;

    public LogAspect(SysLogDao sysLogDao)
    {
        this.sysLogDao = sysLogDao;
    }

    @Pointcut("@annotation(com.springboot.annotation.Log)")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point)
    {
        Object result = null;
        long beginTime = System.currentTimeMillis();
        try
        {
            // 执行方法
            result = point.proceed();
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
        // 执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        // 保存日志
        saveLog(point, time);
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, long time)
    {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        SysLog sysLog = new SysLog();
        Log logAnnotation = method.getAnnotation(Log.class);
        if (logAnnotation != null)
        {
            // 注解上的描述
            sysLog.setOperation(logAnnotation.value());
        }
        // 请求的方法名
        String className = point.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName + "()");
        // 请求的方法参数值
        Object[] args = point.getArgs();
        // 请求的方法参数名称
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        if (args != null && paramNames != null)
        {
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < args.length; i++)
            {
                params.append(" ").append(paramNames[i]).append(": ").append(args[i]);
            }
            sysLog.setParams(params.toString());
        }
        // 获取 request
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        // 设置 IP 地址
        sysLog.setIp(IPUtils.getIpAddr(request));
        // 模拟一个用户名 admin
        sysLog.setUsername("admin");
        sysLog.setTime((int) time);
        sysLog.setCreateTime(new Date());
        // 保存系统日志
        sysLogDao.saveSysLog(sysLog);
    }
}
```

**测试**

TestController：

```java
@RestController
@RequestMapping("/api/v1/aop")
public class TestController
{
    @Log("执行方法一")
    @GetMapping("/one")
    public void methodOne(@RequestParam String name) { }

    @Log("执行方法二")
    @GetMapping("/two")
    public void methodTwo() throws InterruptedException
    {
        Thread.sleep(2000);
    }

    @Log("执行方法三")
    @GetMapping("/three")
    public void methodThree(@RequestParam String name, @RequestParam String age) { }
}
```

最终项目目录如下图所示：

![](http://image.berlin4h.top/images/2020/08/13/20200813153801.png)

启动项目，分别访问：

http://localhost:8072/web/api/v1/aop/one?name=one

http://localhost:8072/web/api/v1/aop/two

http://localhost:8072/web/api/v1/aop/three?name=three&age=1

查询数据库：

![](http://image.berlin4h.top/images/2020/08/13/20200813153933.png)

