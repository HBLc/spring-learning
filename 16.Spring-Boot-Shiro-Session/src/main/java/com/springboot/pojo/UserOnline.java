package com.springboot.pojo;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Description: UserOnline
 *
 * @author hbl
 * @date 2021/04/30 0030 14:00
 */
@Data
@RequiredArgsConstructor
public class UserOnline implements Serializable
{
    private static final long serialVersionUID = 478344940871043529L;

    /**
     * sessionID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户主机地址
     */
    private String host;

    /**
     * 用户登录时系统IP
     */
    private String systemHost;

    /**
     * 状态
     */
    private String status;

    /**
     * session创建时间
     */
    private Date startTimestamp;

    /**
     * session最后访问时间
     */
    private Date lastAccessTime;

    /**
     * 超时时间
     */
    private Long timeout;
}
