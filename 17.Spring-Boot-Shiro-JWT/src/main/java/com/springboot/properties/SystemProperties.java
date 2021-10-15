package com.springboot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description: SystemProperties
 *
 * @author hbl
 * @date 2021/05/27 0027 15:56
 */
@Data
@Component
@ConfigurationProperties(prefix = "system")
public class SystemProperties
{
    /**
     * 免认证 URL
     */
    private String anonUrl;

    /**
     * token默认有效时间
     */
    private Long jwtTimeOut = 86400L;
}
