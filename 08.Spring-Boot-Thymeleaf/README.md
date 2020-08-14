###  Spring Boot 中使用 Thymeleaf

Spring Boot 支持 FreeMarker、Groovy、Thymeleaf 和 Mustache 四种模板解析引擎，官方推荐使用 Thymeleaf。

**spring-boot-starter-thymeleaf**

在 Spring Boot 中使用 Thymeleaf 只需要在 pom 中加入 Thymeleaf 的 starter 即可：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

在 Spring Boot 中，默认的 html 页面地址为 src/main/resources/templates，默认静态资源地址为 src/main/resources/static。

**Thymeleaf 默认配置**

在 Spring Boot 配置文件中可对 Thymeleaf 的默认配置进行修改：

```yml
spring:
  thymeleaf:
    # 开启模板缓存 (默认值: true)
    cache: true
    check-template: true
    # 检查模板位置是否正确 (默认值:true)
    check-template-location: true
    servlet:
      # Content-Type 的值 (默认值: text/html)
      content-type: text/html
    # 开启 MVC Thymeleaf 视图解析 (默认值: true)
    enabled: true
    # 模版编码
    encoding: UTF-8
    # 要被排除在解析之外的视图名称列表, 用逗号分隔
    excluded-view-names:
    # 要运用于模板之上的模板模式. 另见 StandardTemplate-ModeHandlers (默认值: HTML)
    mode: HTML
    # 在构建 URL 时添加到视图名称前的前缀 (默认值: classpath:/templates/)
    prefix: classpath:/templates/
    # 在构建 URL 时添加到视图名称后的后缀 (默认值: .html)
    suffix: .html
    # Thymeleaf 模板解析器在解析器链中的顺序. 默认情况下, 它排第一位. 顺序从1开始, 只有在定义了额外的 TemplateResolver Bean 时才需要这个属性
    template-resolver-order:
    # 可解析的视图名称列表, 用逗号隔开
    view-names:
```

一般开发中将 spring.thymeleaf.cache 设置为 false，其他保持默认即可。

**简单示例**

编写一个简单的 Controller：

```java
@Controller
@RequestMapping("/api/v1/thymeleaf")
public class IndexController
{
    @GetMapping("/account")
    public String index(Model m)
    {
        List<Account> list = new ArrayList<>();
        list.add(new Account("账户A", "AAA", "password_A", "admin", "123456789"));
        list.add(new Account("账户B", "BBB", "password_B", "user", "987654321"));
        m.addAttribute("accountList", list);
        return "account";
    }
}
```

编写 account.html 页面：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>account</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" th:href="@{/css/style.css}" type="text/css">
</head>
<body>
    <table>
        <tr>
            <th>no</th>
            <th>account</th>
            <th>name</th>
            <th>password</th>
            <th>accountType</th>
            <th>tel</th>
        </tr>
        <tr th:each="list,stat : ${accountList}">
            <td th:text="${stat.count}"></td>
            <td th:text="${list.account}"></td>
            <td th:text="${list.name}"></td>
            <td th:text="${list.password}"></td>
            <td th:text="${list.accountType}"></td>
            <td th:text="${list.tel}"></td>
        </tr>
    </table>
</body>
</html>
```

最终项目目录结构如下图所示：

![](http://image.berlin4h.top/images/2020/08/14/20200814171628.png)

启动项目，访问 http://localhost:8080/api/v1/thymeleaf/account

![](http://image.berlin4h.top/images/2020/08/14/20200814171754.png)

