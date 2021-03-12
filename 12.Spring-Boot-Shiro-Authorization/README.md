### Spring Boot Shiro 权限控制

在上一个项目《Spring-Boot-Shiro用户认证》中，我们通过继承 AuthorizingRealm 抽象类实现了 `doGetAuthenticationIngo()` 方法完成了用户认证操作。接下来继续实现 `doGetAuthorizationInfo()` 方法完成 Shiro 的权限控制功能。

![](http://image.berlin4h.top/images/2021/03/12/20210312110605.png)

可以看到：应用代码直接交互的对象是Subject，也就是说Shiro的对外API核心就是Subject；其每个API的含义：

**Subject：**主体，代表了当前“用户”，这个用户不一定是一个具体的人，与当前应用交互的任何东西都是Subject，如网络爬虫，机器人等；即一个抽象概念；所有Subject都绑定到SecurityManager，与Subject的所有交互都会委托给SecurityManager；可以把Subject认为是一个门面；SecurityManager才是实际的执行者；

**SecurityManager：**安全管理器；即所有与安全有关的操作都会与SecurityManager交互；且它管理着所有Subject；可以看出它是Shiro的核心，它负责与后边介绍的其他组件进行交互，如果学习过SpringMVC，你可以把它看成DispatcherServlet前端控制器；

**Realm：**域，Shiro从Realm获取安全数据（如用户、角色、权限），就是说SecurityManager要验证用户身份，那么它需要从Realm获取相应的用户进行比较以确定用户身份是否合法；也需要从Realm得到用户相应的角色/权限进行验证用户是否能进行操作；可以把Realm看成DataSource，即安全数据源。

授权也称为访问控制，是管理资源访问的过称。即根据不同的用户判断是否有访问相应资源的权限。在 Shiro 中，权限控制有三个核心的元素：权限、角色和用户。

#### 库模型设计

在这里，我们使用 `RBAC(Role-Based Access Control, 基于角色的访问控制)`模型设计用户，角色和权限间的关系。简单的说，一个用户拥有若干角色，每个角色拥有若干权限，这样就构造成“用户-角色-权限”的授权模型。在这种模型中，用户和角色之间，角色和权限之间，一般是多对多的关系，如下图所示：

![](http://image.berlin4h.top/images/2020/12/28/RBAC.png)

根据这个模型，设计数据库表，并插入一些测试数据：

```sql
-- ----------------------------
-- Table structure for t_permission
-- ----------------------------
DROP TABLE IF EXISTS `t_permission`;
CREATE TABLE `t_permission`  (
  `ID` int(10) NOT NULL,
  `URL` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'URL地址',
  `NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'URL描述',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_permission
-- ----------------------------
INSERT INTO `t_permission` VALUES (1, '/user', 'user:user');
INSERT INTO `t_permission` VALUES (2, '/user/add', 'user:add');
INSERT INTO `t_permission` VALUES (3, '/user/delete', 'user:delete');

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role`  (
  `ID` int(11) NOT NULL,
  `NAME` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `MEMO` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色描述',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_role
-- ----------------------------
INSERT INTO `t_role` VALUES (1, 'admin', '超级管理员');
INSERT INTO `t_role` VALUES (2, 'test', '测试账户');

-- ----------------------------
-- Table structure for t_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `t_role_permission`;
CREATE TABLE `t_role_permission`  (
  `RID` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  `PID` int(11) NULL DEFAULT NULL COMMENT '权限ID'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_role_permission
-- ----------------------------
INSERT INTO `t_role_permission` VALUES (1, 1);
INSERT INTO `t_role_permission` VALUES (1, 2);
INSERT INTO `t_role_permission` VALUES (1, 3);
INSERT INTO `t_role_permission` VALUES (2, 1);

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `ID` int(11) NOT NULL,
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `passwd` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'mrbird', '8e8a1a7b18d6486793b4bc96346a3345', '2017-11-19 10:52:48', '1');
INSERT INTO `t_user` VALUES (2, 'test', '4d1eb74a4c2e0593704b66a0f1013c6f', '2017-11-19 17:20:21', '0');

-- ----------------------------
-- Table structure for t_user_role
-- ----------------------------
DROP TABLE IF EXISTS `t_user_role`;
CREATE TABLE `t_user_role`  (
  `USER_ID` int(11) NULL DEFAULT NULL COMMENT '用户ID',
  `RID` int(11) NULL DEFAULT NULL COMMENT '角色ID'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user_role
-- ----------------------------
INSERT INTO `t_user_role` VALUES (1, 1);
INSERT INTO `t_user_role` VALUES (2, 2);
```

上面的 sql 创建了五张表：用户表 **T_USER** 、角色表 **T_ROLE** 、用户角色关联表 **T_USER_ROLE** 、权限表 **T_PERMISSION** 和权限角色关联表 **T_ROLE_PERMISSION**。用户 mrbird 角色为 admin，用户 test 角色为 test。admin 角色拥有用户的所有权限（user:user，user:add，user:delete），而 test 角色只拥有用户的查看权限（user:user）。密码都是 test，经过 Shiro 提供的 MD5 加密；

#### Dao 层

创建两个实体类，对应用户角色表 Role 和用户权限表 Permission：

Role：

```java
@Data
public class Role implements Serializable {
    private static final long serialVersionUID = -1;
    private Integer id;
    private String name;
    private String memo;
}
```

Permission：

```java
public class Permission implements Serializable {
    private static final long serialVersionUID = -1;
    private Integer id;
    private String url;
    private String name;
}
```

创建两个 dao 接口，分别用户查询用户的所有角色和用户的所有权限：

UserRoleMapper：

```java
@Mapper
public interface UserRoleMapper {
    List<Role> findByUserName(String userName);
}
```

UserPermissionMapper：

```java
@Mapper
public interface UserPermissionMapper {
    List<Permission> findByUserName(String userName);
}
```

其 xml 实现：

UserRoleMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.springboot.dao.UserRoleMapper">
    <resultMap id="role" type="com.springboot.pojo.Role">
        <id column="id" property="id" javaType="java.lang.Integer" jdbcType="NUMERIC"/>
        <id column="name" property="name" javaType="java.lang.String" jdbcType="VARCHAR"/>
        <id column="memo" property="memo" javaType="java.lang.String" jdbcType="VARCHAR"/>
    </resultMap>

    <select id="findByUserName" resultMap="role">
        select r.id, r.name, r.memo from t_role r
        left join t_user_role ur on r.id = ur.rid
        left join t_user u on u.id = ur.user_id
        where u.username = #{userName}
    </select>
</mapper>
```

UserPermissionMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.springboot.dao.UserPermissionMapper">
    <resultMap id="permission" type="com.springboot.pojo.Permission">
        <id column="id" property="id" javaType="java.lang.Integer" jdbcType="NUMERIC"/>
        <id column="url" property="url" javaType="java.lang.String" jdbcType="VARCHAR"/>
        <id column="name" property="name" javaType="java.lang.String" jdbcType="VARCHAR"/>
    </resultMap>

    <select id="findByUserName" resultMap="permission">
        select p.id, p.url, p.name from t_role r
        left join t_user_role ur on r.id = ur.rid
        left join t_user u on u.id = ur.user_id
        left join t_role_permission rp on rp.rid = r.id
        left join t_permission p on p.id = rp.pid
        where u.username = #{userName}
    </select>
</mapper>

```

数据层准备好后，接下来对 Realm 进行改造。

#### Realm

在 Shiro 中，用户角色和权限的获取是在 Realm 的 `doGetAuthorizationInfo()` 方法中实现的，所以接下来手动实现该方法：

```java
public class ShiroRealm extends AuthorizingRealm {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private UserPermissionMapper userPermissionMapper;
    
    // 获取用户角色和权限
    @Overirde
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal)
    {
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        String userName = user.getUserName();
        
        System.out.println("用户" + userName + "获取权限----ShrioRealm.doGetAuthorizationInfo");
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        
        // 获取用户角色集
        List<Role> roleList = userRoleMapper.findByUserName(userName);
        Set<String> roleSet = new HashSet<String>();
        for (Role r : roleList) {
            roleSet.add(r.getName());
        }
        simpleAuthorizationInfo.setRoles(roleSet);
        
        // 获取用户权限集
        List<Permission> permissionList = userPermissionMapper.findByUserName(userName);
        Set<String> permissionSet = new HashSet<String>();
        for (Permission p : permissionList) {
            permissionSet.add(p.getName());
        }
        simpleAuthorizationInfo.setStringPermission(permissionSet);
        return simpleAuthorizationInfo;
    }
    
    // 登录认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
    {
        // 登录认证已经实现过，这里不再贴代码
	}
}
```

在上述代码中，我们通过方法 `userMapper.findByUserName(userName)` 和 `userPermissionMapper.findByUserName(userName)` 获取了当前登录用户的角色和权限集，然后保存到 SimpleAUthorizationInfo 对象中，并返回给 Shiro，这样 Shiro 中就存储了当前用户的角色和权限信息了。除了对 Realm 进行改造外，我们还需要修改 ShiroConfig 配置。

#### ShiroConfig

Shiro 为我们提供了一些权限相关注解，如下所示：

```java
// 表示当前 Subject 已经通过 login 进行了身份验证; 即 Subject.isAuthenticated() 返回 true
@RequiresAuthentication
// 表示当前 Subject 已经身份验证或者通过记住我登录的
@RequiresUser
// 表示当前 Subject 没有身份验证或者通过记住我登录过, 既是游客身份
@RequiresGuest
// 表示当前 Subject 需要角色 admin 和 user
@RequiresRoles(value={"admin", "user"}, logical=Logical.AND)
// 表示当前 Subject 需要权限 user:a 或者 user:b
@RquiresPermissions(value={"user:a", "user:b"}, logical=Logical.OR)
```

要开启这些注解的使用，需要在ShiroConfig中添加如下配置：

```java
...
@Bean
public AUthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
    AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
    authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
    return authorizaationAttributeSourceAdvisor;
}
...
```

#### Controller

编写一个 UserController，用于处理 User 类的访问请求，并使用Shiro权限注解控制权限：

```java
@Controller
@RequestMapping("/api/v1/user")
public class UserController{
    @RequiresPermissions("user:user")
    @RequestMapping("/list")
    public String userList(Model model) {
        model.addAttribute("value", "获取用户信息");
        return "user";
    }
    
    @RequiresPermissions("user:add")
    @RequestMapping("add")
    public String userAdd(Model model) {
        model.addAttribute("value", "新增用户");
        return "user";
    }
    
    @RequiresPermissions("user:delete")
    @RequestMapping("delete")
    public String userDelete(Model model) {
        model.addAttribute("value", "删除用户");
        return "user";
    }
}
```

在 LoginController 中添加一个 /403 跳转

```java
@GetMapping("/403")
public String forbid() {
    return "403";
}
```

#### 前端页面

对 index.html 进行改造，添加三个用户操作的链接：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>首页</title>
</head>
<body>
    <p>你好~[[${user.userName}]]</p>
    <h3>权限测试链接</h3>
    <div>
        <a th:href="@{/user/list}">获取用户信息</a>
        <a th:href="@{/user/add}">新增用户</a>
        <a th:href="@{/user/delete}">删除用户</a>
    </div>
    <a th:href="@{/logout}">注销</a>
</body>
</html>
```

当用户对用户的操作有相应的权限的时候，跳转到 user.html：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>[[${value}]]</title>
    </head>
    <body>
        <p>[[${value}]]</p>
        <a th:href="@{/index}">返回</a>
    </body>
</html>
```

403 页面：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
    <p>您没有权限访问该资源~</p>
    <a th:href="@{/index}">返回</a>
</html>
```

#### 测试

启动项目，使用 mrbird 的账户登录后主页如下图所示：

![](http://image.berlin4h.top/images/2021/03/11/20210311172428.png)

点击“获取用户信息链接”：

![](http://image.berlin4h.top/images/2021/03/11/20210311172439.png)

点击“新增用户链接”：

![](http://image.berlin4h.top/images/2021/03/11/20210311172453.png)

因为 mrbird 角色为admin，对这三个链接都有访问权限。

接着使用 test 用户登录，因为 test 用户的角色为 test，只拥有（user:user）权限，所以当其点击“新增用户”和“删除用户”的时候：

![image-20210311175806691](C:\Users\My\AppData\Roaming\Typora\typora-user-images\image-20210311175806691.png)

后台抛出 `AuthorizationException: Not authorized to invoke method: public java.lang.String com.springboot.controller.UserController.userDelete` 异常！！！

这里有点出乎意料，本以为在 ShiroConfig 中配置了 shiroFilterFactoryBean.setUnauthorizedUrl("/403");，没有权限的访问会自动重定向到 /403，结果证明并不是这样。后来研究发现，该设置只对 filterChain 起作用，比如在 filterChain 中设置了 filterChainDefinitionMap.put("/user/update", "perms[user:update]");，如果用户没有 user:update 权限，那么当其访问 /user/update 的时候，页面会被重定向到 /403。

那么对于上面这个问题，我们可以定一个以全局异常捕获类：

```java
@ControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler
{
    @ExceptionHandler(value = AuthorizationException.class)
    public String handleAuthorizationException() {
        return "403";
    }
}

```

启动项目，再次使用test帐号点击“新增用户”和“删除用户”链接时，页面如下所示：

![](http://image.berlin4h.top/images/2021/03/11/20210311181020.png)

页面已经成功重定向到 /403。
