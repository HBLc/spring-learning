package com.springboot.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Description: SysLog
 *
 * @author hbl
 * @date 2020/8/13 0013 14:09
 */
@Data
public class SysLog implements Serializable
{
    private static final long serialVersionUID = -6309732882044872298L;

    private Integer id;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作
     */
    private String operation;

    /**
     * 响应时间
     */
    private Integer time;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 请求IP
     */
    private String ip;

    /**
     * 创建时间
     */
    private Date createTime;
}
