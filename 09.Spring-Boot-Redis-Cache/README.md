### Spring Boot 中使用缓存

在程序中可以使用缓存的技术来节省对数据库的开销。Spring Boot 对缓存提供了很好的支持，我们几乎不用做过多的配置即可使用各种缓存实现。这里主要介绍平日里个人接触较多的 Ehcache 和 Redis 缓存实现。

**准备工作**

可根据 03.Spring-Boot 整合 MyBatis 搭建一个 Spring Boot 项目，然后 yml 中配置日志输出级别以观察 SQL 的执行情况：

```yml
logging:
	level:
		com.springboot.mapper: debug
```

其中 com.springboot.mapper 为 MyBatis 的 Mapper 接口路径。

然后编写如下测试方法：

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = SpringBootRedisCacheApplication.class)
class SpringBootRedisCacheApplicationTests
{
    @Resource
    private StudentService service;

    @Test
    void contextLoads()
    {
        Student student1 = this.service.queryStudentBySno("001");
        System.out.println("学号为" + student1.getSno() + "的学生姓名姓名: " + student1.getName());

        Student student2 = this.service.queryStudentBySno("001");
        System.out.println("学号为" + student2.getSno() + "的学生姓名姓名: " + student2.getName());
    }
}
```

点击 IDEA 左侧运行：

![image-20200824171646825](C:\Users\My\AppData\Roaming\Typora\typora-user-images\image-20200824171646825.png)

![](http://image.berlin4h.top/images/2020/08/24/20200824171503.png)

可发现第二个查询虽然和第一个查询完全一样，但其还是对数据库进行了查询。接下来引入缓存来改善这个结果。

**使用缓存**

要开启 Spring Boot 的缓存功能，需要在 pom 中引入 `spring-boot-starter-cache`：

```xml
<!-- 引入缓存 -->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

接着在 Spirng Boot 入口类中加入 `@EnableCaching` 注解开启缓存功能：

```java
@EnableCaching
@SpringBootApplication
@MapperScan("com.springboot.mapper")
public class SpringBootRedisCacheApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(SpringBootRedisCacheApplication.class, args);
    }
}
```

在 StudentService 接口中加入缓存注解：

```java
@CacheConfig(cacheNames = "student")
public interface StudentService
{
    @CachePut(key = "#p0.sno")
    Student update(Student student);

    @CacheEvict(key = "#p0", allEntries = true)
    int deleteBySno(String sno);

    @Cacheable(key = "#p0")
    Student queryStudentBySno(String sno);
}
```

我们在 StudentService 接口中加入了 `@CacheConfig` 注解，queryStudentBySnoo 方法使用了注解 `@Cacheable(key = "#p0")`，即将 id 作为 redis 中的 key 值。当我们更新数据的时候，应该使用 `@CachePut(key = #p0.sno)` 进行缓存数据的更新，否则将查询到脏数据，因为该注解保存的是方法的返回值，所以这里应该返回 Student。当我们删除数据的时候，应该使用 `@CacheEvict(key = "#p0", allEntries = true)` 来标注需要清除的缓存 key；allEntries 表示是否需要清除缓存中所有的元素，当指定了 `allEntries = true` 时将忽略指定的 key。

其实现类：

```java
@Service("studentService")
public class StudentServiceImpl implements StudentService
{
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public Student update(Student student)
    {
        this.studentMapper.update(student);
        return this.studentMapper.queryStudentBySno(student.getSno());
    }

    @Override
    public int deleteBySno(String sno)
    {
        return this.studentMapper.deleteBySno(sno);
    }

    @Override
    public Student queryStudentBySno(String sno)
    {
        return this.studentMapper.queryStudentBySno(sno);
    }
}
```

在 Spring Boot 中可以使用的缓存注解有：

- 缓存注解

1. `@CacheConfig`：主要用于配置该类中会用到的一些公用的缓存配置。在这里 `@CacheConfig(cacheNames = "student")`：配置了该数据访问对象中返回的内容将存储于名为 student 的缓存对象中，我们也可以不使用该注解，直接通过 `@Cacheable` 自己配置缓存集的名字来定义；
2. `@Cacheable`：配置了 queryStudentBySno 函数的返回值将被加入缓存。同时在查询时，会先从缓存中获取，若不存在才发起对数据库的访问。该注解主要有下面几个参数：
   - value、cacheNames：两个等同的参数（cacheNames 为 Spring 4 新增，作为 value 的别名），用于指定缓存存储的集合名。由于 Spring 4 中新增了 `@CacheConfig`，因此在 Spring 3 中原本必须有的 value 属性，也成为了非必须项了；
   - key：缓存对象存储在 Map 集合中的 key 值，非必需，缺省按照函数的所有参数组合作为 key 值，若自己配置需使用 SpEL 表达式，比如`：@Cacheable(key = "#p0")`：使用函数的第一个参数多位缓存的 key 值，更多关于 SpEL 表达式的详细内容可参考 https://docs.spring.io/spring/docs/current/spring-framework-reference/integration.html#cache；
   - condition：缓存对象的条件，非必需，也需要使用 SpEL 表达式，只有满足表达式条件的内容才会被缓存，比如：`@Cacheable(key = "#p0", condition = "p0.length() < 3")`，表示只有当地一个参数的长度小于 3 的时候才会被缓存；
   - unless：另外一个缓存条件参数，非必需，需要使用 SpEL 表达式。它不同于 condition 参数的地方在于它的判断时机，该条件是在函数被调用之后才做判断的，所以它可以通过对 result 进行判断；
   - keyGenerator：用于指定 key 生成器，非比需。若需要指定一个自定义 key 生成器，我们需要去实现 `org.springframework.cache.interceptor.KeyGenerator` 接口，并使用该参数来指定；
   - cacheManager：用于指定使用哪个缓存管理器，非比需。只有当有多个时才需要使用；
   - cacheResolver：用于指定使用哪个缓存解析器，非比需。需通过 `org.springframework.cache.interceptor.CacheResolver` 接口来实现自己的缓存解析器，并使用该参数指定；
3. `@CachePut`：配置于函数上，能够根据参数定义条件来进行缓存，其缓存的是方法的返回值，它与 `@Cacheable` 不同的是，他每次都会真实调用函数，所以主要用于数据新增和修改操作上。它的参数与 `@cacheable` 类似，具体功能可以参考上面对 `@Cacheable` 参数的解析；
4. `@CacheEvict`：配置于函数上，通常在删除方法上，用来从缓存中移除相应的数据。除了同 `@Cacheable` 一样的参数之外，它还有下面两个参数：
   - allEntries：非比需，默认为 false。当为 true 时，会移除所有数据；
   - beforeInvocation：非比需，默认为 false，会在调用方法之后移除数据。当为 true 时，会在调用方法之前移除数据。

- 缓存实现


要使用上 Spring Boot 的缓存功能，还需要提供一个缓存的具体实现。Sping Boot 根据厦门的顺序去侦测缓存实现：

1. Generic
2. JCache (JSR-107)
3. EhCache 2.x
4. Hazelcast
5. Infinispan
6. Redis
7. Guava
8. Simple

除了按顺序侦测外，我们也可以通过配置属性 `spring.cache.type` 来强制指定。

接下来主要介绍基于 Redis 和 EhCache 的缓存实现。

- Redis


Redis 的[下载地址](https://github.com/MicrosoftArchive/redis/releases)，Redis 支持 32 位和 64位。这个需要根据你系统平台的实际情况选择，这里我们下载 Redis-x64-xxx.zip 压缩包。下载后解压进入并打开一个 CMD 窗口，输入如下命令：

![](http://image.berlin4h.top/images/2020/09/15/20200915144505.png)

然后打开另一个 CMD 终端，输入：

![](http://image.berlin4h.top/images/2020/09/15/20200915144543.png)

准备工作做完后，接下来开始在 Spring Boot 项目里引入 Redis：

```xml
<!-- spring boot redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

在 application.yml 中配置 Redis：

```yml
spring:
  redis:
    # Redis 数据库索引（默认为0）
    database: 0
    # Redis 服务器地址
    host: localhost
    # Redis 服务器连接端口
    port: 6379
    jedis:
      pool:
        # 连接池最大连接数（使用负值表示没有限制）
        max-active: 8
        # 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-wait: -1ms
        # 连接池中最大空闲连接
        max-idle: 8
        # 连接池中最小空闲连接
        min-idle: 0
    # 连接超时时间（毫秒）
    timeout: 0
```

更多关于 Spring Boot Redis 配置可 [参考](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html#%20REDIS)

接着创建一个 Redis 配置类：

```Java
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
    /**
     * 自定义缓存 Key 生成策略
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuffer sb = new StringBuffer();
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object param : params) {
                    sb.append(param.toString());
                }
                return sb.toString();
            }
        };
    }

    /**
     * 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                // 设置过期时间
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)))
                .transactionAware()
                .build();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        // 设置序列化工具
        setSerializer(template);
        template.afterPropertiesSet();
        return template;
    }

    private void setSerializer(StringRedisTemplate template) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
    }
}
```

运行测试，控制台输出：

![](http://image.berlin4h.top/images/2020/09/15/20200915155515.png)

第二次查询没有访问数据库，而是从缓存中获取的，在 Redis 中查看该值：

![](http://image.berlin4h.top/images/2020/09/15/20200915155802.png)

在测试方法中测试更新：

```java
@Test
public void test2 throws Exception {
    Student student1 = this.service.queryStudentBySno("001");
        System.out.println("学号为" + student1.getSno() + "的学生姓名姓名: " + student1.getName());

        student1.setName("修改姓名");
        this.service.update(student1);

        Student student2 = this.service.queryStudentBySno("001");
        System.out.println("学号为" + student2.getSno() + "的学生姓名姓名: " + student2.getName());
}
```

控制台输出：

![](http://image.berlin4h.top/images/2020/09/15/20200915160415.png)

在 Redis 中查看到不同，缓存也得到了更新。

![](http://image.berlin4h.top/images/2020/09/15/20200915160613.png)



**Ehcache**

引入 Ehcache 依赖：

```xml
<!-- ehcache -->
<dependency>
    <groupId>net.sf.ehcache</groupId>
    <artifactId>ehcache</artifactId>
</dependency>
```

在 src/main/resources 目录下新建 ehcache.xml：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="ehcache.xsd">
    <defaultCache
        maxElementsInMemory="10000"
        eternal="false"
        timeToIdleSeconds="3600"
        timeToLiveSeconds="0"
        overflowToDisk="false"
        diskPersistent="false"
        diskExpiryThreadIntervalSeconds="120"/>

    <cache
        name="student"
        maxEntriesLocalHeap="2000"
        eternal="false"
        timeToIdleSeconds="3600"
        timeToLiveSeconds="0"
        overflowToDisk="false"
        statistics="true"/>
</ehcache>
```

关于 Ehcache 的一些说明：

- name：缓存名称
- maxElementslnMemory：缓存最大数目
- maxElementsOnDisk：硬盘最大缓存个数
- eternal：对象是否永久有效，一旦设置了，timeout 将不起作用
- overflowToDisk：是否保存到磁盘
- timeToldeSeconds：设置对象在失效前的允许闲置时间（单位：秒）。仅当 `eternal=false` 对象不是永久有效时使用，可选属性，默认值是 0，也就是可以闲置时间无穷大
- timeToLiveSeconds：设置对象在失效前的允许存活时间（单位：秒）。最大时间介于创建时间和失效时间之间。仅当 `eternal=false` 对象不是永久有效时使用，默认是0，也就是对象存活时间无穷大
- diskPersistent：是否缓存虚拟机重启期数据，默认值为 false
- diskSpoolBufferSizeMB：这个参数设置 DiskStore （磁盘缓存）的缓存区大小。默认是 30MB。每个 Cache 都应该有自己的一个缓冲区。
- diskExpiryThreadIntervalSeconds：磁盘失效线程运行时间间隔，默认是 120 秒
- memoryStoreEvictionPolicy：当达到 maxElementsInMemory 限制时，Ehcache 将会根据指定的策略去清理内存。默认策略是 LRU（最近最少使用）。你可以设置为 FIFO（先进先出）或是 LFU（较少使用）
- clearOnFlush：内存数量最大时是否清除
- memoryStoreEvictionPolicy：Ehcache 的三种清空策略：`FIFO(first in first out)`，这个是大家最熟悉的，先进先出。`LFU(Less Frequently Used)`，就是上面例子中使用的策略，直白一点讲就是最少被使用的。如上面所讲，缓存的元素有一个 hit 属性，hit 值最小的将会被清出缓存。`LRU(Least Recently Used)`，最近最少使用的，缓存的元素有一个时间戳，当缓存容量满了，而又需要腾出地方来缓存新的元素的时候，那么现有缓存元素中时间戳离当前时间最远的元素将被清出缓存。

接着在 application.yml 中指定 ehcache 配置的路径：

```yml
spring:
  cache:
    ehcache:
      config: 'classpath:ehcache.xml'
```

这样就可以开始使用 ehcache 了，运行测试类，观察控制台：

![](http://image.berlin4h.top/images/2020/09/15/20200915172957.png)

可以看到第二次是从缓存中获取的

测试更新：

![](http://image.berlin4h.top/images/2020/09/15/20200915173151.png)

可见，即使更新方法加了 @CachePut 注解，第二次查询因为 Student 对象更新了，其实从数据库获取数据的，所以对于 Ehcache 来说，更新的方法加不加 @CachePut 注解结果都是一样的。