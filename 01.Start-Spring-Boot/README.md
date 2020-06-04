#### 开启 `Spring Boot`

`Spring Boot` 是在 `Spring` 框架上创建的一个全新的框架，但其设计的目的是简化 `Spring` 应用的搭建和开发过程。开启 `Spring Boot` 有许多种方法可供选择，这里仅介绍使用 `IDEA` 来构建一个简单的 `Spring Boot` 项目。

##### `IDEA` 点击左上角文件新建新的项目（注：这里使用的IDEA 2020.1.1 版本，使用了官方的汉化插件）

![](http://fp1.fghrsh.net/2020/05/30/139b527d222c5baf85a92528bcd72bff.png)

##### 选择 `Spring Initializr` 来构建一个简单的 `Spring Boot` 项目。

![](http://fp1.fghrsh.net/2020/05/30/d2398b5ad0959bfbfc80c3f5c331703d.png)

##### 点击下一步设置项目属性，选择构建为 `Maven Project` ，打包方式为 `jar`

![](http://fp1.fghrsh.net/2020/05/30/5fc6ed5c639e20c4f66401277b333776.png)

##### 点击下一步选择项目 `Spring Boot` 版本和依赖，这里仅选择 `web` 进行演示

![](http://fp1.fghrsh.net/2020/05/30/d697fb390bcf5dcaa78a1b207a713bc2.png)

##### 点击下一步，设置项目生成名称和位置，点击完成即可完成创建

![](http://fp1.fghrsh.net/2020/05/30/326f104159e30d751c29b601aa35ad2b.png)

##### 项目根目录下生成了一个 `artifactId+Application` 命名的入口类，生成的项目结构如下（注：删除了部分不需要的文件）

![](http://fp1.fghrsh.net/2020/05/30/6d53e35f75dea0335f4b354b4dbee4ef.png)

##### 简单演示，直接在入口类中编写代码：

```java
package com.springboot.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class DemoApplication
{

    @GetMapping("/")
    public String index()
    {
        return "hello spring boot";
    }

    public static void main(String[] args)
    {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```

##### 然后用 `IDEA` 启动该项目，或者在入口类右键选择运行该入口类，或者在 `Main` 方法左侧点击运行

![](http://fp1.fghrsh.net/2020/05/30/cd316b63934e10cf54ea5b03ee324f64.png)

##### 浏览器访问 `localhost:8080` 可得到如下结果：

![](http://fp1.fghrsh.net/2020/05/30/ac79e129c8e3f68c62dccf670c6853fd.png)

##### 打包发布，在 `IDEA` 右侧 `maven` 栏中选中项目生命周期打包，或在终端使用 `mvn clean package`命令打包，打包成功后会在 `target` 目录下生成一个 `jar` 文件

![](http://fp1.fghrsh.net/2020/05/30/6d5f17eceff31d0cefa5e200e8e31766.png)

##### 生成的 `jar` 可使用 `java -jar` 命令启动

![](http://fp1.fghrsh.net/2020/05/30/f1633f121c7a69906184d0ecd246b285.png)

##### 访问 `localhost:8080` 效果如上，该启动方式在终端按下 `ctrl+c` 结束运行。



#### 聊聊 `pom.xml`

##### 打开 `pom.xml` 可看到配置如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.0.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.springboot</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>开启 Spring Boot</description>
    <packaging>jar</packaging>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

```

##### `spring-boot-start-parent` 指定了当前项目是一个 Spring Boot 项目，它提供了诸多的默认 `Maven` 依赖，具体可以查看 `spring-boot-dependencies-2.3.0.RELEASE.pom` 文件，可以使用 `IDEA` 找到该文件，操作如下：

打开项目的 `pom.xml` 文件，按住 `Ctrl` 键鼠标左键单击 `spring-boot-starter-parent` 即可进入这个父项目的 `pom` 配置文件：

![](http://fp1.fghrsh.net/2020/06/03/8726514a1286680c9135694564e867bc.png)

然后我们再以同样的手法点击 `spring-boot-dependencies` 即可进入这个父项目的 `pom` 配置文件，可以看到该配置里面引用了很多的依赖，这里仅截取一小部分：

```xml
<properties>
    <activemq.version>5.15.12</activemq.version>
    <antlr2.version>2.7.7</antlr2.version>
    <appengine-sdk.version>1.9.80</appengine-sdk.version>
    ...
    <spring-ldap.version>2.3.3.RELEASE</spring-ldap.version>
    <spring-restdocs.version>2.0.4.RELEASE</spring-restdocs.version>
    <spring-retry.version>1.2.5.RELEASE</spring-retry.version>
    <spring-security.version>5.3.2.RELEASE</spring-security.version>
    <spring-session-bom.version>Dragonfruit-RELEASE</spring-session-bom.version>
    <spring-ws.version>3.0.9.RELEASE</spring-ws.version>
    ...
    <webjars-hal-browser.version>3325375</webjars-hal-browser.version>
    <webjars-locator-core.version>0.45</webjars-locator-core.version>
    <wsdl4j.version>1.6.3</wsdl4j.version>
    <xml-maven-plugin.version>1.0.2</xml-maven-plugin.version>
    <xmlunit2.version>2.7.0</xmlunit2.version>
  </properties>
```

需要说明的是，并非所有在 `<properties>` 标签中配置了版本号的依赖都有被启用，其启用与否取决于您的项目中是否配置了相应的 `starter`。比如 `tomcat` 这个依赖就是 `spring-boot-starter-web` 的依赖传递性（下面将会描述到）。当然，我们可以手动改变这些依赖的配置/版本。比如我们想把 `thymeleaf` 的版本改为 `3.0.0.RELEASE`，我们可以在自己的项目 `pom.xml` 中配置：

```xml
<properties>
    <thymeleaf.version>3.0.0.RELEASE</thymeleaf.version>
</properties>
```



##### `spring-boot-starter-web`

`Spring Boot` 提供了许多开箱即用的依赖模块，这些模块都是以 `spring-boot-starter-XXX` 命名的。比如要开启 `Spring Boot` 提供的 `web` 功能，只需要在项目的 `pom.xml` 中配置 `spring-boot-starter-web` 即可：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

因为 `web` 模块依赖于 `spring-boot-starter-parent`，所以这里可以不用配置 `version`。保存后 `Maven` 会自动帮我们下载 `spring-boot-starter-web` 模块所包含的 `jar` 文件。如果需要具体查看 `spring-boot-starter-web` 包含了哪些依赖，我们可以通过 `IDEA` 查看到该模块的依赖，只需要按住 `Ctrl` 鼠标左键点击 `spring-boot-starter-web` 即可看到该模块的 `pom` 配置，依赖如下：

```xml
<dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
      <version>2.3.0.RELEASE</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-json</artifactId>
      <version>2.3.0.RELEASE</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-tomcat</artifactId>
      <version>2.3.0.RELEASE</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-web</artifactId>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-webmvc</artifactId>
      <scope>compile</scope>
    </dependency>
  </dependencies>
```

上述这些依赖都是隐式依赖于 `spring-boot-starter-web`，我们也可以手动排除一些我们不需要的依赖。比如 `spring-boot-starter-web` 默认集成了 `tomcat`，加入我们想把它换为 `jetty`，可以在项目 `pom.xml` 中 `spring-boot-starter-web` 下排除 `tomcat` 依赖，然后手动引入 `jetty` 依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

可以看到项目的外部依赖已改变，`tomcat` 已被替换为 `jetty`

![](http://fp1.fghrsh.net/2020/06/03/d577bb3667b4af8930874ab09638286e.png)

启动项目同样可以看到 `web` 容器  `tomcat` 已被替换为 `jetty `

#### `spring-boot-maven-plugin`

`spring-boot-maven-plugin` 为 `Spring Boot Maven` 插件，提供了：

1、把项目打包成一个可执行的超级 `JAR（uber-JAR）`，包括把应用程序的所有依赖打入 `JAR` 文件内，并为 `JAR` 添加一个描述文件，其中的内容能让你用 `java -jar` 来运行程序。

2、搜索 `public static void main()` 方法来标记为可运行类。