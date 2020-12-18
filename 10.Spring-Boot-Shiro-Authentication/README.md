### Spring Boot Shiro 用户认证

在 Spring Boot 中集成 Shiro 进行用户的认证过程主要可以归纳为以下三点：

1. 定义一个 ShiroConfig，然后配置 SecurityManager Bean，SecurityManager 为 Shiro 的安全管理器，管理着所有的 Subject；
2. 在 ShiroConfig 中配置 ShiroFilterFactoryBean，其为 Shiro 过滤器工厂类，依赖于 SecurityManager；
3. 自定义 Realm 实现，Realm 包含 `doGetAuthorizationInfo()` 和 `doGetAuthenticationInfo()`方法，因为文本只涉及用户认证，所以只实现 `doGetAuthenticationInfo()` 方法。

##### 引入依赖

首先搭建一个 Spring Boot Web 程序，然后引入 Shiro、MyBatis、数据库和 Thymeleaf 依赖：

```xml
<!-- shiro-spring -->
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-spring</artifactId>
    <version>1.4.0</version>
</dependency>
<!-- Oracle 驱动 -->
<dependency>
    <groupId>com.oracle</groupId>
    <artifactId>ojdbc6</artifactId>
    <version>6.0</version>
</dependency>
<!-- Druid 数据源驱动 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.1.22</version>
</dependency>
<!-- thymeleaf -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<!-- MyBatis -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.1.3</version>
</dependency>
```

##### ShiroConfig

定义一个 Shiro 配置类，名称为 ShiroConfig：

```java
@Configuration
public class ShiroConfig {
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 设置 securityManager
        shiroFilterFactoryBean.setSecurityManager(secutiryManager);
        // 登录的 url
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后跳转的 url
        shiroFilterFactoryBean.setSuccessUrl("/index");
        // 未授权 url
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        
        // 定义 filterChain, 静态资源不拦截
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        // druid 数据源监控页面不拦截
        filterChainDefinitionMap.put("/druid/**", "anon");
        // 配置退出过滤器, 其中具体的退出代码 Shiro 已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        filterChainDefinitionMap.put("/", "anon");
        
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }
    
    @Bean
    public SecurityManager securityManager() {
        // 配置 SecurityManager, 并注入 shiroRealm
        DefaultWebSecutiryManager secutiryManager = new DefaultWebSecurityManager();
        secutiryManager.setRealm(shiroReal());
        return securityManager;
    }
    
    @Bean
    public ShiroRealm shiroRealm() {
        // 配置 Realm, 需要自己实现
        ShiroRealm shiroRealm = new ShiroRealm();
        return shiroRealm;
    }
}
```

需要注意的是 filterChain 基于短路机制，即最先匹配原则，如：

```
/user/**=anon
/user/aa=authc 永远不会执行
```

其中 anon、authc 等为 Shiro 为我们实现的过滤器，具体如下表所示：

| Filter Name        | Class                                                        | Description                                                  |
| ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| anon               | [org.apache.shiro.web.filter.authc.AnonymousFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authc/AnonymousFilter.html) | 匿名拦截器，即不需要登录即可访问；一般用于静态资源过滤；示例 /static/**=anon |
| authc              | [org.apache.shiro.web.filter.authc.AnonymousFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authc/AnonymousFilter.html) | 基于表单的拦截器；如 /**=authc，如果没有登录会跳转到相应的登录页面登录 |
| authcBasic         | [org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authc/BasicHttpAuthenticationFilter.html) | Basic HTTP 身份验证拦截器                                    |
| logout             | [ org.apache.shiro.web.filter.authc.LogoutFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authc/LogoutFilter.html) | 推出拦截器，主要属性：redirectUrl：退出成功后重定向的地址(/)，示例 /logout=logout |
| nooSessionCreation | [org.apache.shiro.web.filter.session.NoSessionCreationFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/session/NoSessionCreationFilter.html) | 不会创建会话拦截器，调用 subject.getSession(false)不会有什么问题，但是如果 subject.getSession(true) 将抛出 DisabledSessionException  异常 |
| perms              | [org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authz/PermissionsAuthorizationFilter.html) | 权限授权拦截器，验证用户是否拥有所有权限；属性和 roles一样；示例 /user/**=perms["user:create"] |
| port               | [org.apache.shiro.web.filter.authz.PortFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authz/PortFilter.html) | 端口拦截器，主要属性port(80)：可以通过端口；示例 /test=port[80]，如果用户访问该页面是非80，将自动将请求端口改为80并重定向到该80端口，其他路径/参数等 |
| rest               | [org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authz/HttpMethodPermissionFilter.html) | rest 风格拦截器，自动根据请求方法构建权限字符串；示例 /users=rest[user]，会自动拼出 user:read,user:create,user:update,user=delete权限字符串进行权限匹配（所有都得匹配，isPermittedAll） |
| roles              | [org.apache.shiro.web.filter.authz.RolesAuthorizationFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authz/RolesAuthorizationFilter.html) | 角色授权拦截器，验证用户是否拥有所有角色；示例 /admin/**=roles[admin] |
| ssl                | [ org.apache.shiro.web.filter.authz.SslFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authz/SslFilter.html) | SSL 拦截器，只有协议是 https 才能通过；否则会自动跳转 https 端口443；其他和 port 拦截器一样； |
| user               | [org.apache.shiro.web.filter.authc.UserFilter](http://shiro.apache.org/static/current/apidocs/org/apache/shiro/web/filter/authc/UserFilter.html) | 用户拦截器，用户已经身份验证/记住我的登录的都可以；示例 /**=user |

配置完 ShiroConfig 后，接下来对 Realm 进行实现，然后注入到 SecurityManager 中。

##### Realm

自定义 Realm 实现只需要继承 AuthorizingRealm 类，然后实现 doGetAurhorizationIfo() 和 doGetAuthenticationInfo() 方法即可。这两个方法名乍看有点像，authorization 为授权、批准的意思，即获取用户的角色和权限等信息；authentication 为认证、身份验证的意思，即登录时验证用户的合法性，比如验证用户名和密码。

```java
public class ShirRealm extends AuthorizingRealm {
    @Autowired
    private UserMapper userMapper;
    
    // 获取用户角色和权限
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollextion principal) {
        return null;
    }
    
    // 登录认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        // 获取用户输入的用户名和密码
        String userName = (String) token.getPrincipal();
        String password = new String((char[]) token.getCredentials());
        
        System.out.println("用户" + userName + "认证-----ShiroRealm.doGetAuthenticationInfo()");
        
        // 通过用户名到数据库查询用户信息
        User user = userMapper.findBuUserName(userName);
        
        if (user == null) {
            throw new UnknownAccountException("用户名或密码错误！");
        }
        if (!password.equals(user.getPassword())) {
            throw new IncorrectCredentialsException("用户名或密码错误！");
        }
        if (user.getStatus().equals("0")) {
            throw new LockedAccountException("帐号已被锁定, 请联系系统管理员！");
        }
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, password, getName());
        return info;
    }
}
```

因为本节只讲述用户认证，所以 doGetAuthorizationInfo() 方法先不进行实现。

其中 UnknowAccountException 等异常为 Shiro 自带异常，Shiro 具有丰富的运行时 AuthenticationException 层次结构，可以准确的指出异常失败的原因。你可以包装在一个 try/catch 块，并捕捉任何你希望的异常，并作出相应的反应。例如：

```java
try {
    currentUser.login(token);
} catch (UnknowAccountException uae) { ...
} catch (IncorrectCredentialsException ice) { ...
} catch (LockedAccountException lae) { ...
} catch (ExcessliveAttemptsException eae) { ...
} ... catch you own ...
} catch (AuthenticationException ae) {
    // unexpected error?
}
```

虽然我们可以准确的捕获异常信息，并根据这些信息给用户提示具体的错误，但是最安全的做法是在登录失败时仅向用户显示通用的错误提示信息，例如"用户名或密码错误"，这样可以防止数据库被恶意扫描。

在 Realm 中 UserMapper 为 Dao 层，标准的做法应该还有 Service 层，但这里为了方便就不再定义 Service 层，接下来编写和数据库打交道的 Dao 层。

##### 数据层

首先创建一张用户表，用于存储用户的基本信息（基于 Oracle 11g）：

```sql
CREATE TABLE "T_USER" (
	"ID" NUMBER NOT NULL,
    "USERNAME" VARCHAR2(20 BYTE) NOT NULL,
    "PASSWD" VARCHAR2(128 BYTE) NOT NULL,
    "CREATE_TIME" DATE NULL,
    "STATUS" CHAR(1 BYTE) NOT NULL
);

COMMENT ON COLUMN "T_USER"."USERNAME" IS '用户名';
COMMENT ON COLUMN "T_USER"."PASSWD" IS '密码';
COMMENT ON COLUMN "T_USER"."CREATE_TIME" IS '创建时间';
COMMENT ON COLUMN "T_USER"."STATUS" IS '是否有效 1:有效 0:锁定';

INSERT INTO "T_USER" VALUES ('1', 'test1', '42ee25d1e43e9f57119a00d0a39e5250', TO_DATE('2017-11-19 10:52:48', 'YYYY-MM-DD HH24:MI:SS'), '1');
INSERT INTO "T_USER" VALUES ('2', 'test2', '7a38c13ec5e9310aed731de58bbc4214', TO_DATE('2017-11-19 17:20:21', 'YYYY-MM-DD HH24:MI:SS'), '0');

ALTER TABLE "T_USER" ADD PRIMARY KEY ("ID");
```

数据源的配置这里就不再贴出来了，具体可参考源码；

库对应的实体类：

```java
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String userName;
    private String password;
    private Date createTime;
    private String status;
}
```

定义接口 UserMapper：

```java
@Mapper
public interface UserMapper {
    User findByUserName(String userName);
}
```

xml 实现：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.springboot.dao.UserMapper">

    <resultMap type="com.springboot.pojo.User" id="User">
        <id column="id" property="id" javaType="java.lang.Integer" jdbcType="NUMERIC"/>
        <id column="username" property="userName" javaType="java.lang.String" jdbcType="VARCHAR"/>
        <id column="passwd" property="password" javaType="java.lang.String" jdbcType="VARCHAR"/>
        <id column="create_time" property="createTime" javaType="java.util.Date" jdbcType="DATE"/>
        <id column="status" property="status" javaType="java.lang.String" jdbcType="VARCHAR"/>
    </resultMap>

    <select id="findByUserName" resultMap="User">
        select * from t_user where username = #{userName}
    </select>

</mapper>
```

数据层准备完了，接下来编写 login.html 和 index.html 页面。

**页面准备**

编写登录页面 login.html：

```html
<head>
    <meta charset="UTF-8">
    <title>登录</title>
    <link rel="stylesheet" th:href="@{/css/login.css}" type="text/css">
    <script th:src="@{/js/jquery-1.11.1.min.js}"></script>
</head>
<body>
    <div class="login-page">
        <div class="form">
            <input type="text" placeholder="用户名" name="username" required="required"/>
            <input type="password" placeholder="密码" name="password" required="required"/>
            <button onclick="login()">登录</button>
        </div>
    </div>
</body>
<script th:inline="javascript">
    var ctx = [[@{}]];
    function login() {
        var username = $("input[name='username']").val();
        var password = $("input[name='password']").val();
        $.ajax({
            type: "post",
            url: ctx + "login",
            data: {"username": username, "password": password},
            dataType: "json",
            success: funcation (r) {
            	if (r.code == 0) {
            		location.href = ctx + 'index';
		        } else {
                    alter(r.msg);
                }
	        }
        });
    }
</script>
```

主页 index.html：

```html
<head>
    <meta charset="UTF-8">
    <title>首页</title>
</head>
<body>
    <p>你好![[${user.userName}]]</p>
    <a th:href="@{/logout}">住校</a>
</body>
```

页面准备完毕，接下来编写 LoginController。

##### Controller

LoginController 代码如下：

```java
@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @PostMapping("/login")
    @ResponseBody
    public ResponseBo login(String username, String password) {
        // 密码 MD5 加密
        password = MD5utils.encrypt(username, password);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        // 获取 Subject 对象
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            return ReponseBo.ok();
        } catch (UnknownAccountException e) {
            return ResponseBo.error(e.getMEssage());
        } catch (IncorrectCredentialsException e) {
            return ResponseBo.error(e.getMessage);
        } catch (LockedAccountException e) {
            return ResponseBo.error(e.getMessga);
        } catch (AuthenticationException e) {
            return ResponseBo.error("认证失败~");
        }
    }
    
    @RequestMapping("/")
    public String redirectIndex() {
        return "redirect:/index";
    }
    
    @RequestMapping("/index")
    public String index(Model model) {
        // 登录成功后，即可通过 Subject 获取登录的用户信息
        User user = (User) SecurityUtils.getSUbject().getPrincipal();
        model.addAttribute("user", user);
        return "index";
    }
}
```

登录成功之后，根据之前在 ShiroConfig 中配置的 `shiroFilterFactoryBean.setSuccessUrl("/index")`，页面会自动访问 `/index` 路径。

**测试**

最终项目目录结构如下图所示：

启动项目，分别访问：

- http://localhost:8080/web/
- http://localhost:8080/web/index
- http://localhost:8080/web/aaaaaa
- http://localhost:8080/web

可发现页面都被重定向到 http://localhost:8080/web/login：

![](http://image.berlin4h.top/images/2020/12/18/20201218141820.png)

当输入错误的用户信息时：

![](http://image.berlin4h.top/images/2020/12/18/20201218141921.png)

用 test 的账户登录（test 账户的 	status 为 0，已被锁定）：

![](http://image.berlin4h.top/images/2020/12/18/20201218143019.png)

当输入正确的用户名密码时候：

![](http://image.berlin4h.top/images/2020/12/18/20201218143134.png)

点击注销连接，根据 ShiroConfig 配置的 `filterChainDefinitionMap.put("/logout", "logout")`，Shiro 会自动帮我们注销用户信息，并重定向到 / 路径。

Spring Boot 集成 Shiro 进行用户认证就到此结束了；


