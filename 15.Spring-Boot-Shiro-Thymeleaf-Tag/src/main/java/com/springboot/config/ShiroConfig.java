package com.springboot.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.springboot.shiro.ShiroRealm;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

/**
 * Description: ShiroConfig
 *
 * @author hbl
 * @date 2021/03/11 0011 13:59
 */
@Configuration
public class ShiroConfig
{
    @Bean
    public ShiroDialect shiroDialect()
    {
        return new ShiroDialect();
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager)
    {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSuccessUrl("/index");
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");

        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        // anon 匿名拦截器，即不需要登录即可访问；一般用于静态资源过滤
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        filterChainDefinitionMap.put("/druid/**", "anon");
        // logout 退出拦截器，主要属性：redirectUrl; 退出成功后重定向的地址（/），示例/logout=logout
        filterChainDefinitionMap.put("/logout", "logout");
        filterChainDefinitionMap.put("/", "anon");
        // user 用户拦截器，用户已经身份验证/记住我登录的都可
        filterChainDefinitionMap.put("/**", "user");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }

    @Bean
    public SecurityManager securityManager()
    {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(shiroRealm());
        securityManager.setRememberMeManager(rememberMeManager());
        // 设置EhCache作为缓存
        securityManager.setCacheManager(getEhCacheManager());
        return securityManager;
    }

    // LifecycleBeanPostProcessor将Initializable和Destroyable的实现类统一在其内部自动分别调用了Initializable.init()和Destroyable.destroy()方法，从而达到管理shiro bean生命周期的目的
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor()
    {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public ShiroRealm shiroRealm()
    {
        ShiroRealm shiroRealm = new ShiroRealm();
        return shiroRealm;
    }

    public CookieRememberMeManager rememberMeManager()
    {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        // rememberMe cookie加密的密钥
        cookieRememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return cookieRememberMeManager;
    }

    public SimpleCookie rememberMeCookie()
    {
        // 设置cookie名称, 对应login.html页面的<input type="checkbox" name="rememberMe">
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        // 设置cookie的过期时间, 单位为秒, 这里为一天
        cookie.setMaxAge(86400);
        return cookie;
    }

    /**
     * 开启注解权限需要这个Bean
     * #@RequiresAuthentication 表示当前Subject已经通过login进行了身份验证；即Subject.isAuthenticated()返回true
     * #@RequiresUser 表示当前Subject已经身份验证或者通过记住我登录的
     * #@RequiresGuest 表示当前Subject没有身份验证或者通过记住我登录过，即是游客身份
     * #@RequiresRoles(value={"admin", "user"}, logical=Logical.AND) 表示当前Subject需要角色admin和user
     * #@RequiresPermission(value={"user:a", "user:b"}, logical=Logical.OR) 表示当前Subject需要权限user:a或user:b
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager)
    {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * 解决Shiro注解无效的问题
     */
    @Bean
    @ConditionalOnMissingBean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator()
    {
        DefaultAdvisorAutoProxyCreator app = new DefaultAdvisorAutoProxyCreator();
        app.setProxyTargetClass(true);
        return app;
    }

    // ======================整合EhCache缓存==============================
    @Bean
    public EhCacheManager getEhCacheManager()
    {
        EhCacheManager em = new EhCacheManager();
        em.setCacheManagerConfigFile("classpath:config/shiro-ehcache.xml");
        return em;
    }
}
