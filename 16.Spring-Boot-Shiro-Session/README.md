### Spring Boot Shiro 在线会话管理

在 Shiro 中我们可以通过 `org.apache.shiro.session.mgt.eis.SessionDAO` 对象的 `getActiveSession()` 方法方便的获取到当前所有有效的 `Session` 对象。通过这些 `Session` 对象，我们可以实现一些比较有趣的功能，比如查看当前西戎在线人数，查看这些用户的一些基本信息，强制让某个用户下线等。

为了达到这几个目标，我们现有的 Spring Boot Shiro 项目基础上进行一些改造（缓存使用Ehcache）。

#### 更改 ShiroConfig

为了能够在 Spring Boot 中使用 SessionDao，我们在 ShiroConfig 中配置改Bean：

```java
@Bean
public SessionDAO sessionDAO() {
    MemorySessionDAO sessionDAO = new MemorySessionDAO();
    return sessionDAO;
}
```

如果使用的是 Redis 作为缓存实现，那么 SessionDAO 则作为 `RedisSessionDAO`：

```java
@Bean
public RedisSessionDAO sessionDAO() {
    RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
    redisSessionDAO.setRedisManager(redisManager());
    return redisSessionDAO;
}
```

在 Shiro 中，SessionDao 通过 `org.apache.shiro.session.mgt.SessionManager` 进行管理，所以继续在 ShiroConfig 中配置 SessionManager：

```java
@Bean
public SessionManager sessionManager() {
    DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
    Collection<SessionListener> listeners = new ArrayList<SessionListener>();
    listeners.add(new ShiroSessionListener());
    sessionManager.setSessionListeners(listeners);
    sessionManager.setSessionDAO(sessionDAO());
    return sessionManager;
}
```

其中 ShiroSessionListener 为 `org.apache.shiro.session.SessionListener` 接口的手动实现，所以接下来定义一个该接口的实现：

```java
public class ShiroSessionListener implements SessionListener {
    private final AtomicInteger sessionCount = new AtomicInteger(0);
    
    @Override
    public void onStart(Session session) {
        sessionCount.incrementAndGet();
    }
    
    @Override
    public void onStop(Session session) {
        sessionCount.decrementAndGet();
    }
    
    @Override
    public void onExpiration(Session session) {
        sessionCount.decrementAndGet();
    }
}
```

其维护着一个原子类型的 Integer 对象，用于统计在线 Session 的数量。

定义完 SessionManager 后，还需将其注入到 SecurityManager 中：

```java
@Bean
public SecurityManager securityManager() {
    DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
    securityManager.setRealm(shiroRealm());
    ...
    securityManager.setSessionManager(sessionManager());
    return securityManager;
}
```

#### UserOnline

配置完 ShiroConfig 后，我们可以创建一个 UserOnline 实体类，用于描述每个在线用户的基本信息：

```java
@Data
public class UserOnline implements Serializable {
    private static final long serialVersionUID = 1;
    // session id
    private String id;
    // 用户id
    private String userId;
    // 用户昵称
    private String username;
    // 用户主机地址
    private String host;
    // 用户登录时系统IP
    private String systemHost;
    // 状态
    private String status;
    // session创建时间
    private Date startTimestamp;
    // session最后访问时间
    private Date lastAccessTime;
    // 超时时间
    private Long timeout;
}
```

#### Service

创建一个 Service 接口，包含查看所有在线用户和根据 SessionId 踢出用户抽象方法：

```java
public interface SessionService {
    List<UserOnline> list();
    boolean forceLogout(String sessionId);
}
```

其具体实现：

```java
@Service("sessionService")
public class SessionServiceImpl implements SessionService {
    @Autowired
    private SessionDAO sessionDAO;
    
    @Override
    public List<UserOnline> list() {
        List<UserOnline> list = new ArrayList<>();
        Collection<Session> sessions = sessionDAO.getActiveSessions();
        for (Session session : sessions) {
            UserOnline userOnline = new UserOnline();
            User user = new User();
            SimplePrincipalCollection principalCollection = new SimplePrincipalCollection();
            if (session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null) {
                continue;
            } else {
                principalCollection = (SimplePrincipalCollection) session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
                user = (User) principalCollection.getPrimaryPrincipal();
                userOnline.setUsername(user.getUserName);
                userOnline.setUserId(user.getId().toString());
            }
            userOnline.setId((String) session.getId());
            userOnline.setHost(session.getHost());
            userOnline.setStartTimestamp(session.getStartTimestamp());
            userOnline.setLastAccessTime(session.getLastAccessTime());
            Long timeout = session.getTimeout();
            if (timeout == 0l) {
                userOnline.setStatus("离线");
            } else {
                userOnline.setStatus("在线");
            }
            userOnline.setTimeout(timeout);
            list.add(userOnline);
        }
        return list;
    }
    
    @Override
    public boolean forceLogout(String sessionId) {
        Session session = sessionDAO.readSession(sessionId);
        session.setTimeout(0);
        return true;
    }
}
```

通过 SessionDao 的 `getActiveSessions()` 方法，我们可以获取所有有效的 Session，通过该 Session，我们还可以获取到当前用户的 Principal 信息。

值得说明的是，当某个用户被踢出后（Session Timeout置为0），该 Session 并不会立刻从 ActiveSessions 中剔除，所以我们可以通过其 timeout 信息来判断该用户在线与否。

如果使用的 Redis 作为缓存的实现，那么 `forceLogout()` 方法需要稍作修改：

```java
@Override
public boolean forceLogout(String sessionId) {
    Session session = sessionDAO.readSession(sessionId);
    sessionDAO.delete(session);
    return true;
}
```

#### Controller

定义一个 SessionController，用于处理 Session 的相关操作：

```java
@Controller
@RequestMapping("/online")
public class SessionController {
    @Autowired
    SessionService sessionService;
    
    @RequestMapping("/index")
    public String online() {
        return "online";
    }
    
    @ResponseBody
    @RequestMapping("/list")
    public List<UserOnline> list() {
        return sessionService.list();
    }
    
    @ResponseBody
	@RequestMapping("forceLogout")
	public ResponseBo forceLogout(String id) {
		try {
			sessionService.forceLogout(id);
			return ResponseBo.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseBo.error("踢出用户失败");
		}

	}
}
```

#### 页面

我们编写一个 online.html 页面，用于展示所有在线用户的信息：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>在线用户管理</title>
    <script th:src="@{/js/jquery-1.11.1.min.js}"></script>
    <script th:src="@{/js/dateFormat.js}"></script>
</head>
<body>
    <h3>在线用户数：<span id="onlineCount"></span></h3>
    <table>
        <tr>
            <th>序号</th>
            <th>用户名称</th>
            <th>登录时间</th>
            <th>最后访问时间</th>
            <th>主机</th>
            <th>状态</th>
            <th>操作</th>
        </tr>
    </table>
    <a th:href="@{/index}">返回</a>
</body>
<script th:inline="javascript">
    var ctx = [[@{/}]];
    $.get(ctx + "api/v1/online/list", {}, function(r){
        var length = r.length;
        $("#onlineCount").text(length);
        var html = "";
        for(var i = 0; i < length; i++){
            html += "<tr>"
                + "<td>" + (i+1) + "</td>"
                + "<td>" + r[i].username + "</td>"
                + "<td>" + new Date(r[i].startTimestamp).Format("yyyy-MM-dd hh:mm:ss") + "</td>"
                + "<td>" + new Date(r[i].lastAccessTime).Format("yyyy-MM-dd hh:mm:ss") + "</td>"
                + "<td>" + r[i].host + "</td>"
                + "<td>" + r[i].status + "</td>"
                + "<td><a href='#' onclick='offline(\"" + r[i].id + "\",\"" + r[i].status +"\")'>下线</a></td>"
                + "</tr>";
        }
        $("table").append(html);
    },"json");
	
    function offline(id,status){
        if(status == "离线"){
            alert("该用户已是离线状态！！");
            return;
        }
        $.get(ctx + "api/v1/online/forceLogout", {"id": id}, function(r){
            if (r.code == 0) {
                alert('该用户已强制下线！');
                location.href = ctx + 'api/v1/online/index';
            } else {
                alert(r.msg);
            }
        },"json");
    }
</script>
</html>
```

在 inde.html 中加入该页面的入口：

```html
...
<body>
    <p>你好！[[${user.userName}]]</p>
    <p shiro:hasRole="admin">你的角色为超级管理员</p>
    <p shiro:hasRole="test">你的角色为测试账户</p>
    <div>
        <a shiro:hasPermission="user:user" th:href="@{/user/list}">获取用户信息</a>
        <a shiro:hasPermission="user:add" th:href="@{/user/add}">新增用户</a>
        <a shiro:hasPermission="user:delete" th:href="@{/user/delete}">删除用户</a>
    </div>
    <a shiro:hasRole="admin" th:href="@{/api/v1/online/index}">在线用户管理</a>
    <a th:href="@{/logout}">注销</a>
</body>
...
```

#### 测试

启动项目，在浏览器中使用 mrbird 账户访问：

![](http://image.berlin4h.top/images/2021/05/07/20210507154148.png)

在另一个浏览器中使用 tester 账户访问：

![](http://image.berlin4h.top/images/2021/05/07/20210507154202.png)

然后在 mrbird 主界面点击“在线用户管理”：

![](http://image.berlin4h.top/images/2021/05/07/20210507154213.png)

显示的信息符合我们的预期，点击 tester 的下线按钮，强制将其踢出：

![image-20210507154304285](C:\Users\My\AppData\Roaming\Typora\typora-user-images\image-20210507154304285.png)

回到 tester 用户的主界面，点击“查看用户信息”，会发现页面已经被重定向到 login.html 页面，因为其 Session 已经失效！

再次刷新 mrbird 的 online 页面，显示如下：

![](http://image.berlin4h.top/images/2021/05/07/20210507154334.png)


