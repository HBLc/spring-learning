### Spring Boot Shiro 中使用缓存

在 Shiro 中加入缓存可以使权限相关操作尽可能快，避免频繁的访问数据库取权限信息，因为对于一个用户来说，其权限在段时间内基本是不会变化的。Shiro 提供了 Cache 的抽象，其没有直接提供相应的实现，因为这已经超出了一个安全框架的范围。在 Shiro 中可以集成常用的缓存实现，这里介绍基于 Redis 和 Ehcache 缓存的实现。

在上一篇中，当用户访问“获取用户信息”、“新增用户”和“删除用户”的时候，后台输出了三次打印信息，如下所示：

```shell
用户mrbird获取权限-----ShiroRealm.doGetAuthorizationInfo
用户mrbird获取权限-----ShiroRealm.doGetAuthorizationInfo
用户mrbird获取权限-----ShiroRealm.doGetAuthorizationInfo
```

说明在这三次访问中，Shiro 都会从数据库中获取用户的权限信息，通过 Druid 数据源 SQL 监控后台也可以证实这一点：

![](http://image.berlin4h.top/images/2021/03/12/20210312102600.png)

这对数据库来说是没必要的消耗。接下来可以用缓存来解决这个问题。

#### Redis

- 引入 Redis 依赖

网络上已经有关于 Shiro 集成 Redis 的实现，我们引入即可：

```xml
<dependency>
    <groupId>org.crazycake</groupId>
    <artifactId>shiro-redis</artifactId>
    <version>2.4.2-RELEASE</version>
</dependency>
```

- 配置 Redis

我们在 application.yml 配置文件中加入 Redis 配置：

```yml
spring:
  redis:
    host: localhost
    port: 6379
    pool:
      max-active: 8
      max-wait: -1
      max-idle: 8
      min-idle: 0
    timeout: 0
```

接着在 ShiroConfig 中配置 Redis：

```java
public RedisManager redisManager() {
    RedisManager redisManager = new RedisManager();
    return redisManager;
}

public RedisCacheManager cacheManager() {
    RedisCacheManager redisCacheManager = new RedisCacheManager();
    redisCacheManager.setRedisManager(redisManager());
    return redisCacheManager;
}
```

上面代码配置了 RedisManager，并将其注入到了 RedisCacheManager 中，最后在 SecurityManager 中加入 RedisCacheManager：

```java
@Bean
public SecurityManager securityManager() {
    DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
    ...
    securityManager.setCacheManager(cacheManager());
    return securityManager;
}
```

配置完启动项目，分别访问“获取用户信息”、“新增用户”和“删除用户”，可发现后台只打印一次获取权限信息：

```shell
用户mrbird获取权限-----ShiroRealm.doGetAuthorizationInfo
```

查看 Druid 数据源 SQL 监控：

![image-20210312145124805](C:\Users\My\AppData\Roaming\Typora\typora-user-images\image-20210312145124805.png)

#### Ehcache

- Ehcache 依赖

加入 Ehcache 相关依赖：

```xml
<!-- shiro ehcache -->
<dependedcy>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-ehcache</artifactId>
    <version>1.3.2</version>
</dependedcy>
<!-- ehcache -->
<dependedcy>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependedcy>
<dependedcy>
    <groupId>net.sf.ehcache</groupId>
    <artifactId>ehcache</artifactId>
</dependedcy>
```

- Ehcache 配置

在 src/main/resource/config 路径下新增一个 Ehcache 配置：shiro-ehcache.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="http://ehcache.org/ehcache.xsd"
    updateCheck="false">
    <diskStore path="java.io.tmpdit/Tmp_Ehcache"/>
    <defaultCache
        maxElementsInMemory="10000"
        eternal="false"
        timeToIdleSeconds="120"
        timeToLiveSeconds="120"
        overflowToDisk="false"
        diskPersistent="false"
        diskExpiryThreadIntervalSeconds="120"/>
    
    <!-- 登录记录缓存锁定1小时 -->
    <cache
        name="passwordRetryCache"
        maxEntriesLocalHeap="2000"
        eternal="false"
        timeToIdleSeconds="3600"
        timeToLiveSeconds="0"
        overflowToDisk="false"
        statistics="true"/>
</ehcache>
```

- ShiroConfig 配置 Ehcache

接着在 ShiroConfig 中注入 Ehcache 缓存：

```java
@Bean
public EhCacheManager getEhCacheManager() {
    EhCacheManager em = new EhCacheManger();
    em.setCacheManagerConfigFile("classpath:config/shiro-ehcache.xml");
    return em;
}
```

将缓存对象注入到 SecurityManager 中：

```java
@Bean
public SecurityManager securityManager() {
    DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
    securityManager.setRealm(shiroRealm());
    securityManager.setRememberMeManager(rememberMeManager());
    securityManager.setCacheManager(getEhCacheManager());
    return securityManager;
}
```

配置完启动项目，分别访问“获取用户信息”、“新增用户”和“删除用户”，可发现后台只打印一次获取权限信息：

```shell
用户mrbird获取权限-----ShiroRealm.doGetAuthorizationInfo
```

查看 Druid 数据源 SQL 监控：

![](http://image.berlin4h.top/images/2021/03/12/20210312160118.png)

SQL 只执行了一次，说明缓存成功。