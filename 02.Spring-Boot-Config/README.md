### `Spring Boot` 的一些基础配置

#### 1、定制 `Banner`

`Spring Boot` 项目在启动的时候会有一个默认的启动图案：

```verilog
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.3.0.RELEASE)
```

我们可以把这个图案修改为自己想要的。在项目的配置文件目录 `src/main/resources` 下新建 `banner.txt` 文件，然后将自己需要打印的图片写/粘贴进去即可。`ASCII` 图案可以通过网站 [http://www.network-science.de/ascii/]() 一键生成，这里简单演示生成 `banner.txt` 文件如下：

![](http://fp1.fghrsh.net/2020/06/04/64d0b88e4f8f25698499d2385eef1ab3.png)

控制图即会输入 `banner.txt` 文件内容：

![](http://fp1.fghrsh.net/2020/06/04/78088b2e5a6c6c3f12b89bcb46df8efb.png)

当然如果不想要这部分启动日志，也可以关闭，在 `main` 方法中：

```java
public static void main(String[] args)
    {
        SpringApplication application = new SpringApplication(SpringBootConfigApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }
```



#### 2、全局配置文件

在 `src/main/resource` 目录下， `Spring Boot` 提供了一个名为 `application.properties` 的全局配置文件，可以对一些默认配置进行修改。官方也能够识别  `yml` 格式的配置文件，`properties` 文件内容格式为 `key=value`，`yml` 文件内容基础格式为 `key: value`，因为 `yml` 文件格式展示更加清晰明了，所以这里创建了一个 `application.yml` 配置文件，这个配置文件可以修改所有的官方属性。

##### 自定义属性值

`Spring Boot` 允许我们在配置文件 `application.yml` 中自定义一些属性，比如：

```yml
application:
  name: 小明
  sex: 男
  age: 20
```

定义一个 `XiaoMingBean`，通过 `@Value("${属性名}")` 来加载配置文件中的属性值：

```java
@Data
@Component
public class XiaoMingBean
{
    @Value("${application.name}")
    private String name;

    @Value("${application.sex}")
    private String sex;

    @Value("${application.age}")
    private String age;
}
```

编写 `IndexController`，注入该 `Bean`

```java
@RestController
public class IndexController
{
    private final XiaoMingBean xiaoMingBean;

    public IndexController(XiaoMingBean xiaoMingBean)
    {
        this.xiaoMingBean = xiaoMingBean;
    }

    @GetMapping("/xiao-ming")
    public String xiaoMing()
    {
        return xiaoMingBean.toString();
    }
}
```

启动项目，访问 `http://localhost:8080/xiao-ming`，页面显示如下：

![](https://fp1.fghrsh.net/2020/06/04/f9d9b2b7bff0b61363435c959a4e18ae.png)

在属性非常多的情况下，也可以定义一个和配置文件对应的 `Bean` ：

```java
@Data
@Component
@ConfigurationProperties(prefix = "application")
public class XiaoMingBean
{
    private String name;

    private String sex;

    private String age;
}
```

通过注解 `@ConfigurationProperties(prefix = "application")` 指定了属性的通用前缀，通过前缀加属性名和配置文件的属性名一一对应，同时记得加上注解 `@Component` 代表这是一个 `Spring` 组件，当项目启动的时候会把这个类加载成一个 `Bean` 放到容器中，然后再去解析配置文件和属性一一对应。如果没有添加注解 `@Component` 则需要在 `Spring Boot` 的入口类中加上注解 `@EnableConfigurationProperties({XiaoMingBean.class})` 来启用该配置。

```java
@SpringBootApplication
@EnableConfigurationProperties({XiaoMingBean.class})
public class SpringBootConfigApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(SpringBootConfigApplication.class, args);
    }
}
```

之后便可在 `IndexController` 中注入该 `Bean` ，并使用了：

```java
@RestController
public class IndexController
{
    private final XiaoMingBean xiaoMingBean;

    public IndexController(XiaoMingBean xiaoMingBean)
    {
        this.xiaoMingBean = xiaoMingBean;
    }

    @GetMapping("/xiao-ming")
    public String xiaoMing()
    {
        return xiaoMingBean.toString();
    }
}
```

##### 属性间的引用

在 `application.yml` 配置文件中，各个属性可以相互引用，如下：

```yml
application:
  name: 小明
  sex: 男
  age: 20
  info: ${application.name}-${application.sex}-${application.age}
```



#### 3、自定义配置文件

除了可以在 `application.yml` 里配置属性，我们还可以自定义一个配置文件。在 `src/main/resources` 目录下新建一个 `test.properties` 文件：

```properties
test.name=小红
test.sex=女
test.age=19
```

定义一个对应该配置文件的 `Bean` ：

```java
@Data
@Component
@ConfigurationProperties(prefix = "test")
@PropertySource(value = "classpath:test.properties")
public class XiaoHongBean
{
    private String name;

    private String sex;

    private String age;
}
```

注解 `@PropertySource("classpath:test.properties")` 指定了使用哪个配置文件。



#### 4、通过命令行设置属性值

在运行 `Spring Boot Jar` 文件时，可以使用命令 `Java -jar xxx.jar --server.port=8081` 来改变端口的值。这条命令等价于我们手动到 `application.yml` 中修改，且命令行的设置的属性值优先级最高，如果 `application.yml` 中配置了端口为 `8082`，如用命令行启动指定了端口为 `8081`，那么最终启动成功应用的端口为 `8081`。

如果不想项目的配置被命令修改，可以在入口文件的 `main` 方法中进行如下设置：

```java
public static void main(String[] args)
    {
        SpringApplication application = new SpringApplication(SpringBootConfigApplication.class);
        application.setAddCommandLineProperties(false);
        application.run(args);
    }
```



#### 5、使用 `xml` 配置

虽然 `Spring Boot` 并不推荐我们继续使用 `xml` 配置，但如果出现不得不使用的情况下，`Spring Boot` 允许我们在入口类中通过注解 `@ImportResource({classpath:xxx.xml})` 来引入 `xml` 配置文件。



#### 6、`Profile` 配置

`Profile` 用来针对不同环境下使用不同的配置文件，多环境配置文件必须以 `application-{profile}.yml` 的格式命名，其中 `{profile}` 为环境标识。比如定义两个配置文件：

- `application-dev.yml`：开发环境

  ```yml
  server:
    port: 8080
  ```

- `application-pord.yml`：生产环境

```yml
server:
  port: 8081
```

至于哪个配置文件会被项目所加载，需要在 `application.yml` 文件中通过 `spring.profiles.active` 属性来指定，其值对应 `{profile}` 值。例如启动 `dev` 配置：

```yml
spring:
  profiles:
    active: dev
```

也可以在运行 `jar` 文件的时候使用命令 `java -jar xxx.jar --spring.profiles.active=dev` 来切换不同的配置。

