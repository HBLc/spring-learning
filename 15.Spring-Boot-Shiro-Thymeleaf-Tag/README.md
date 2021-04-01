### Spring Boot Themeleaf 中使用 Shiro 标签

在前几篇 SpringBoot 整合 Shiro 权限控制中，当用户没有访问权限的资源时，我们采取的做法是跳转到 403 页面，但在实际项目中更为常见的做法是只显示当前用户拥有访问权限的资源链接。配合 Thymeleaf 中 Shiro 标签可以很简单的实现这个目标。

实际上 Thymeleaf 官方没有提供 Shiro 的标签，我们需要引入第三方实现，地址为：https://github.com/theborakompanioni/thymeleaf-extras-shiro。

#### 引入 thymeleaf-extras-shiro

在 pom.xml 中引入：

```xml
<dependency>
    <groupId>com.github.theborakompanioni</groupId>
    <artifactId>thymeleaf-extras-shiro</artifactId>
    <version>2.0.0</version>
</dependency>
```

#### ShiroConfig 配置

引入依赖后，需要在 ShiroConfig 中配置该方言标签：

```java
@Bean
public ShiroDialect shiroDialect() {
    return new ShiroDialect();
}
```

#### 首页改造

更改 index.html，用于测试 Shiro 标签的使用：

```html
 <!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" 
      xmlns:shiro="http://www.pollix.at/thymeleaf/shiro" >
<head>
    <meta charset="UTF-8">
    <title>首页</title>
</head>
<body>
    <p>你好！[[${user.userName}]]</p>
    <p shiro:hasRole="admin">你的角色为超级管理员</p>
    <p shiro:hasRole="test">你的角色为测试账户</p>
    <div>
        <a shiro:hasPermission="user:user" th:href="@{/user/list}">获取用户信息</a>
        <a shiro:hasPermission="user:add" th:href="@{/user/add}">新增用户</a>
        <a shiro:hasPermission="user:delete" th:href="@{/user/delete}">删除用户</a>
    </div>
    <a th:href="@{/logout}">注销</a>
</body>
</html>
```

值得注意的是，在 html 页面中使用 Shiro 标签需要给 html 标签添加 `xmlns:shiro="http://www.pollix.at/thymeleaf/shrio"`。

#### 测试

启动项目，使用 mrbird（角色为 admin，具有 user:user，user:add，user:delete 权限）账户登录：

![](http://image.berlin4h.top/images/2021/04/01/20210401152541.png)

使用 test（角色为 test，仅有 user:user 权限）账户登录：

![](http://image.berlin4h.top/images/2021/04/01/20210401152514.png)

#### 更多标签

The following examples show how to integrate the tags in your Thymeleaf templates. These are all implementations of the examples given in the [JSP / GSP Tag Library Section](http://shiro.apache.org/web.html#Web-JSP%2FGSPTagLibrary) of the Apache Shiro documentation.

Tags can be written in attribute or element notation:

- Attribute

```html
<p shiro:anyTag>
    Goodbye cruel World!
</p>
```

- Element

```html
<shiro:anyTag>
    <p>Hello World!</p>
</shiro:anyTag>
```

- The `guest` tag

```html
<p shiro:guest="">
    Please <a href="login.html">Login</a>
</p>
```

- The `user` tag

```html
<p shiro:user="">
    Welcome back John! Not John? Click <a href="login.html">here</a>to login.
</p>
```

- The `authenticated` tag

```html
<a shiro:authenticated="" href="updateAccount.html">Update you contact infomation</a>
```

- The `notAuthenticated` tag

```html
<p shiro:notAuthenticated="">
    Please <a href="login.html">login</a>in order to update your credit card information.
</p>
```

- The `principal` tag

```html
<p>Hello, <span shiro:principal=""></span>, how are you today?</p>
```

or

```html
<p>Hello, <shiro:principal/>, how are you today?</p>
```

Typed principal and principal perperty are also supported.

- The `hasRole` tag

```html
<a shiro:hasRole="admin" href="admin.html">Admin the system</a>
```

- The `lacksRole` tag

```html
<p shiro:lacksRole="admin">
    Sorry, you art not allowed to admin the system.
</p>
```

- The `hasAllRoles` tag

```html
<p shiro:hasAllRoles="developer, project manager">
    You are a developer and a project manage.
</p>
```

- The `hasAnyRoles` tag

```html
<p shiro:hasAnyRoles="delvelop, project manager, admin">
    You are a developo, project manager or admin.
</p>
```

- The `hasPermission` tag

```html
<a shiro:hasPermission="user:create" href="createUser.html">Create a new User</a>
```

- The `lacksPermission` tag

```html
<p shiro:lacksPermission="user:delete">
    Sorry, you are not allowed to delete user accounts.
</p>
```

- The `hasAllPermission` tag

```html
<p shiro:hasAllPermissions="user:create, user:delete">
    You can create and delete users.
</p>
```

- The `hasAnyPermission` tag

```html
<p shiro:hasAnyPermissions="user:create, user:delete">
    You can create or delete users.
</p>
```