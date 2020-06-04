package com.springboot;

import com.springboot.bean.XiaoMingBean;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Description: Spring Boot Config<br>
 *
 * @author hbl
 * @date 2020/6/4 0004 10:24
 */
@SpringBootApplication
@EnableConfigurationProperties({XiaoMingBean.class})
// @ImportResource({"classpath:xxx.xml"})
public class SpringBootConfigApplication
{
    public static void main(String[] args)
    {
        SpringApplication application = new SpringApplication(SpringBootConfigApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.setAddCommandLineProperties(false);
        application.run(args);
        // SpringApplication.run(SpringBootConfigApplication.class, args);
    }
}
