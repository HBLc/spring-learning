## Spring Boot Shiro Remember Me

接着上一篇《Spring-Boot-Shiro用户认证》，当用户成功登录后，关闭浏览器后再次打开浏览器访问 http://localhost:8080/web/index，页面会跳转到登录页，之前的登录会因为浏览器的关闭已经失效。

Shiro 为我们提供了 **Remember Me** 功能，用户的登录状态不会因为浏览器的关闭而失效，直到Cookie过期；

#### 更改 ShiroConfig

继续编辑 `ShiroConfig`，加入以下代码：

```java
public SimpleCookie rememberMeCookie()
{
    // 设置 cookie 名称, 对应 login.html 页面的<input type="checkbox" name="rememberMe"/>
    SimpleCookie cookie = new SimpleCookie("rememberMe");
    // 设置 cookie 的过期时间, 单位为秒, 这里为一天
    cookie.setMaxAge(86400);
    return cookie;
}

public CookieRememberMeManager rememberMeManager() 
{
    CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
    cookieRememberManager.setCookie(rememberMeCookie());
    // rememberMe cookie 加密的密钥
    cookieRememberMeManager.setCipherKey(Base64.decode("123ghngfsads=="));
    return cookieRememberMeManager;
}
```

接下来将 cookie 管理对象设置到 SecurityManager 中：

```java
@Bean
public SecurityManager securityManager()
{
    DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
    securityManager.setRealm(shiroRealm());
    securityManager.setRememberMeManager(rememberMeManager());
    return securityManager;
}
```

最后修改权限配置，将 ShiroFilterFactoryBean 的 `filterChainDefinitionMap.put("/**", "authc");`更改为 `filterChainDefinitionMap.put("/**", "user");` user是指用户认证通过或者配置了 RememberMe 记住用户登录状态后可访问；

#### 更改 login.html

在 login.html 中加入 Remember Me checkbox：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
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
            <p><input type="checkbox" name="rememberMe" />记住我</p>
            <button onclick="login()">登录</button>
        </div>
    </div>
</body>
<script th:inline="javascript">
    var ctx = [[@{/}]];
        function login() {
            var username = $("input[name='username']").val();
            var password = $("input[name='password']").val();
            var rememberMe = $("input[name='rememberMe']"}).is(':checked');
            $.ajax({
                type: "post",
                url: ctx + "login",
                data: {"username": username, "password": password},
                dataType: "json",
                success: function (r) {
                    if (r.code == 0) {
                        location.href = ctx + 'index';
                    } else {
                        alert(r.msg);
                    }
                }
            });
        }
</script>
</html>
```

#### 更改 LoginController

更改LoginController 的 login() 方法：

```java
@PostMapping("/login")
@ResponseBody
public ResponseBo login(String username, String password, Boolean remeberMe)
{
    password = MD5Utils.encrypt(username, password);
    UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
    Subject subject = SecurityUtils.getSubject();
    try {
        subject.login(token);
        return ResponseBo.ok();
    } catch (UnknownAccountException e) {
        return ResponseBo.error(e.getMessage());
    } catch (IncorrectCredentialsException e) {
        return ResponseBo.error(e.getMessage());
    } catch (LockedAccountException e) {
        return ResponseBo.error(e.getMessage());
    } catch (AUthenticationException e) {
        return ResponseBo.error("认证失败!");
    }
}
```

当 rememberMe 参数为 true 时，Shiro 就会帮我们记住用户的登录状态。启动项目即可看到效果；

