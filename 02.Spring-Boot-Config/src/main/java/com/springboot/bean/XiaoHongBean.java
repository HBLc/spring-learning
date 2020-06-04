package com.springboot.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Description: XiaoHongBean
 *
 * @author hbl
 * @date 2020/6/4 0004 10:45
 */
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
