### Spring Boot Shiro 整合 JWT 简单 DEMO





























































#### 1、JWT 简介

JSON WEB TOKEN (JWT) 是一个非常轻巧的规范，这个规范允许我们使用 JWT 在用户和服务之间传递安全可靠的信息；我们利用一定的编码生成 Token，并在 Token 中加入一些非敏感信息，将其传递；可以理解为用于客户端和服务端之间验证的一种技术，取代了之前使用 Session 来验证的方式；

> 为什么不使用 Session？
>
> Session 原理是登录之后客户端和服务端各自保存一个相应的 SessionId，每次客户端发起请求的时候就需要携带这个 SessionId 来进行比对，存在以下缺点：
>
> - Session 在用户请求量大的时候服务器开销太大了
> - Session 不利于搭建服务器集群，因为客户端需要访问原本的那个服务器才能获取对应的 SessionId

JWT 使用的是一种令牌技术，实际接口霍服务间传输过程为一串字符串，该字符串分为三部分

1. Header

   存储两个变量：密钥和算法（也就是将 Header 和 Payload 加密成 Signature）

2. Payload

   存储很多东西，基础信息有如下几个：

   - 签发人：也就是这个令牌归属于哪个用户，一般存储 `userId`
   - 创建时间：也就是这个令牌是什么时候创建的
   - 失效时间：也就是这个令牌什么时候失效
   - 唯一标识：一般可以使用算法生成一个唯一标识

3. Signature

   这个是上面部分经过 Header 中算法加密生成的，用于对比信息，防止篡改 Header 和 Payload

然后将这三个部分的信息经过加密生成一个 `JwtToken` 的字符串，发送给客户端保存在本地；当客户端发起请求时携带这个字符串到服务端（可以是在 `cookie`，可以是在 `header`，可以是在 `localStorage` 中），在服务端进行验证该请求是否合法；

`JWT` 详细介绍参考：[点击跳转](https://www.ruanyifeng.com/blog/2018/07/json_web_token-tutorial.html)

这是一个 `SpringBoot+Shiro+JWT` 的简单 demo，无数据库无 redis，我们规定每次请求时，需要在请求头中带上 `token`，通过 `token` 校验权限，如没有，则说明当前为游客状态（或者是跳转到登录页面请求 login 接口），系统内置模拟了两个用户：

| 用户名 | 密码   | 角色   | 权限                   |
| ------ | ------ | ------ | ---------------------- |
| admin  | 123456 | admin  | "user:add","user:view" |
| scott  | 123456 | regist | "user:view"            |



#### 2、JWTUtil

我们使用 JWT 工具类来生成我们的 token，这个工具类主要有生成 token 和校验 token 两个方法；

生成 token 时，指定 token 的过期时间 `EXPIRE_TIME` 和签名密钥 `SECRET`，然后将 date 和 username 写入 token 中，并使用带有密钥的 HS256 签名算法进行签名；

```java
public static String sign(String username, String secret)
{
    try
    {
        username = StringUtils.lowerCase(username);
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create().withClaim("username", username).withExpiresAt(date).sign(algorithm);
    }
    catch (Exception e)
    {
        log.error("错误: {}", e.getMessage());
        return null;
    }
}
```



#### 3、JWTFilter 过滤器

在之前的文章中，我们使用的是 `Shiro` 默认的权限拦截器 `Filter`，而因为要整合 JWT，所以我们需要定义自己的过滤器 `JWTFilter`，`JWTFilter` 继承了 `BasicHttpAuthenticationFilter` ，并对部分方法进行重写；

该过滤器主要有三步：

1. 验证请求路径是否为需要权限验证路径；
2. 如果是不需要权限校验的请求路径则直接放行，如果需要权限校验的请求路径则执行 shiro 的 login 方法，将 token 提交到 Realm 中进行校验；
3. 如果在 token 校验过程中出现错误，如 token 校验失败，那么我们会将该请求视为认证不通过；

另外，将跨域支持也放到了该过滤器来处理；

```java
@Slf4j
public class JWTFilter extends BasicHttpAuthenticationFilter
{
    private static final String TOKEN = "Token";

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
    {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        SystemProperties properties = SpringContextUtil.getBean(SystemProperties.class);
        String[] anonUrl = StringUtils.splitByWholeSeparatorPreserveAllTokens(properties.getAnonUrl(), ",");

        boolean match = false;
        for (String url : anonUrl)
        {
            if (pathMatcher.match(url, httpServletRequest.getRequestURI()))
            {
                match = true;
            }
        }
        if (match)
        {
            return true;
        }
        if (isLoginAttempt(request, response))
        {
            return executeLogin(request, response);
        }
        return false;
    }

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response)
    {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader(TOKEN);
        return token != null;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response)
    {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader(TOKEN);
        JWTToken jwtToken = new JWTToken(token);
        try
        {
            getSubject(request, response).login(jwtToken);
            return true;
        }
        catch (Exception e)
        {
            log.error("错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception
    {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Method", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个option请求, 这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name()))
        {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}
```



#### 4、Realm 类

依然是我们的自定义 Realm，实现用户身份认证和权限认证；

```java
public class ShiroRealm extends AuthorizingRealm
{
    /**
     * 授权模块
     * @param token toekn
     * @return 权限信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection token)
    {
        // 使用 token 获得 username, 从数据库中查到该用户所拥有的角色和权限存入 SimpleAuthorizationInfo 中
        String username = JWTUtil.getUsername(token.toString());
        User user = SystemUtils.getUser(username);

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

        // 获取用户角色集合（模拟值，实际从数据库中取得）
        simpleAuthorizationInfo.setRoles(user.getRole());

        // 获取用户权限集合（模拟值，实际从数据库中取得）
        simpleAuthorizationInfo.setStringPermissions(user.getPermission());

        return simpleAuthorizationInfo;
    }

    /**
     * 用户认证
     * @param token 身份认证
     * @return 身份认证信息
     * @throws AuthenticationException 认证异常
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
    {
        // 这里的 token 是从 JWTFilter 的 executeLogin 方法传递过来的，已经经过了解密
        String tokenStr = (String) token.getCredentials();

        String username = JWTUtil.getUsername(tokenStr);

        if (StringUtils.isBlank(username))
        {
            throw new AuthenticationException("token校验不通过");
        }

        // 通过用户名查询用户信息
        User user = SystemUtils.getUser(username);

        if (user == null)
        {
            throw new AuthenticationException("用户名或密码错误");
        }
        if (!JWTUtil.verify(tokenStr, username, user.getPassword()))
        {
            throw new AuthenticationException("token校验不通过");
        }

        return new SimpleAuthenticationInfo(token, token, "shiro_realm");
    }
}
```



#### 5、权限控制注解 `@RequiresRoles、@RequiresPermissions`

这两个注解为我们主要的权限控制注解，如：

```java
// 拥有 admin 角色可以访问
@RequiresRoles("admin")
// 拥有 regist 或 admin 角色的可以访问
@RequiresRoles(logical = Logical.OR, value = {"regist", "admin"})
// 拥有 "user:add" 权限才能访问
@RequiresPermissions("user:add")
```

当我们写的接口拥有以上注解时，如果请求没有带 token 或者带了 token 但权限认证不通过，则会报 `UnauthenticateException` 异常，但是我在 `GlobalExceptionHandler` 类对这些异常进行了集中处理；

```java
@Slf4j
@RestControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler
{
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleException(Exception e)
    {
        log.error("系统内部异常, 异常信息: ", e);
        return new Response().message("系统内部异常");
    }

    @ExceptionHandler(value = SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleSystemException(SystemException e)
    {
        log.error("系统错误", e);
        return new Response().message(e.getMessage());
    }

    /**
     * 统一处理请求参数校验（实体对象传参）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response validExceptionHandler(BindException e)
    {
        StringBuilder message = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error : fieldErrors)
        {
            message.append(error.getField()).append(error.getDefaultMessage()).append(",");
        }
        message = new StringBuilder(message.substring(0, message.length() - 1));
        return new Response().message(message.toString());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e)
    {
        log.error("参数校验异常", e);
        // 获取异常信息
        BindingResult bindingResult = e.getBindingResult();
        // 判断异常中是否有错误信息, 如果存在就使用异常中的消息, 否则使用默认消息
        if (bindingResult.hasErrors())
        {
            List<ObjectError> errors = bindingResult.getAllErrors();
            if (!errors.isEmpty())
            {
                // 这里列出了全部的错误参数, 按正常逻辑, 只需要处理第一条错误即可
                FieldError fieldError = (FieldError) errors.get(0);
                return new Response().message(fieldError.getDefaultMessage());
            }
        }
        return new Response().message("参数错误");
    }

    @ExceptionHandler(value = UnauthenticatedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handlerUnauthenticatedException(UnauthenticatedException e)
    {
        log.error("权限不足", e);
    }
}
```

除了以上两种，还有 `@RequiresAuthentication`，`@RequiresUser` 等注解；



#### 6、功能测试

- **登录**：登录接口不带有 token，且不需要校验权限，用户名密码验证通过后返回 token

```java
@RestController
@Validated
@RequiredArgsConstructor
public class LoginController
{
    private final SystemProperties systemProperties;

    @PostMapping("/login")
    public Response login(@NotBlank(message = "{required}") String username,
                          @NotBlank(message = "{required}") String password) throws SystemException
    {
        username = StringUtils.lowerCase(username);
        password = MD5Util.encrypt(username, password);

        final String errorMsg = "用户名或密码错误";
        User user = SystemUtils.getUser(username);

        if (user == null)
        {
            throw new SystemException(errorMsg);
        }
        if (!StringUtils.equals(user.getPassword(), password))
        {
            throw new SystemException(errorMsg);
        }

        // 生成 Token
        String token = JWTUtil.sign(username, password);

        Map<String, Object> userInfo = this.generateUserInfo(token, user);
        return new Response().message("认证成功").data(userInfo);
    }

    /**
     * 生成前端需要的用户信息
     */
    private Map<String, Object> generateUserInfo(String token, User user)
    {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", token);

        user.setPassword("");
        userInfo.put("user", user);
        return userInfo;
    }
}
```

![image-20210830175938060](C:\Users\My\AppData\Roaming\Typora\typora-user-images\image-20210830175938060.png)

- **异常处理**

```java
@Slf4j
@RestControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler
{
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleException(Exception e)
    {
        log.error("系统内部异常, 异常信息: ", e);
        return new Response().message(e.getMessage());
    }

    @ExceptionHandler(value = SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleSystemException(SystemException e)
    {
        log.error("系统错误", e);
        return new Response().message(e.getMessage());
    }

    /**
     * 统一处理请求参数校验（实体对象传参）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response validExceptionHandler(BindException e)
    {
        StringBuilder message = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error : fieldErrors)
        {
            message.append(error.getField()).append(error.getDefaultMessage()).append(",");
        }
        message = new StringBuilder(message.substring(0, message.length() - 1));
        return new Response().message(message.toString());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e)
    {
        log.error("参数校验异常", e);
        // 获取异常信息
        BindingResult bindingResult = e.getBindingResult();
        // 判断异常中是否有错误信息, 如果存在就使用异常中的消息, 否则使用默认消息
        if (bindingResult.hasErrors())
        {
            List<ObjectError> errors = bindingResult.getAllErrors();
            if (!errors.isEmpty())
            {
                // 这里列出了全部的错误参数, 按正常逻辑, 只需要处理第一条错误即可
                FieldError fieldError = (FieldError) errors.get(0);
                return new Response().message(fieldError.getDefaultMessage());
            }
        }
        return new Response().message("参数错误");
    }

    @ExceptionHandler(value = UnauthenticatedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handlerUnauthenticatedException(UnauthenticatedException e)
    {
        log.error("权限不足", e);
    }
}
```

- **权限控制**

```java
@RestController
@RequestMapping("/api/v1/test")
public class TestController
{
    /**
     * 需要登录才能访问
     */
    @GetMapping("/1")
    public String test1()
    {
        return "success";
    }

    /**
     * 需要 admin 角色才能访问
     */
    @GetMapping("2")
    @RequiresRoles("admin")
    public String test2()
    {
        return "scuuess";
    }

    /**
     * 需要 "user:add" 权限才能访问
     */
    @GetMapping("3")
    @RequiresPermissions("user:add")
    public String test3()
    {
        return "success";
    }
}
```

![](http://image.berlin4h.top/images/2021/08/30/20210830180320.png)



















