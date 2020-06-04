package com.springboot.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description: XiaoMingBean
 *
 * @author hbl
 * @date 2020/6/4 0004 10:32
 */
@Data
@Component
@ConfigurationProperties(prefix = "application")
public class XiaoMingBean
{
    // @Value("${application.name}")
    private String name;

    // @Value("${application.sex}")
    private String sex;

    // @Value("${application.age}")
    private String age;

    @Override
    public String toString()
    {
        return "XiaoMingBean{" + "name='" + name + '\'' + ", sex='" + sex + '\'' + ", age='" + age + '\'' + '}';
    }
}
