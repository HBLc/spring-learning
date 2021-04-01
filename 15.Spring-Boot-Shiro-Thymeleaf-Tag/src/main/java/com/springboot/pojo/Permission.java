package com.springboot.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * Description: Permission
 *
 * @author hbl
 * @date 2021/03/11 0011 10:10
 */
@Data
public class Permission implements Serializable
{
    private static final long serialVersionUID = 9050306948027721524L;

    private Integer id;

    private String url;

    private String name;
}
