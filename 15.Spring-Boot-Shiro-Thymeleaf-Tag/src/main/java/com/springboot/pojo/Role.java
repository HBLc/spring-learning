package com.springboot.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * Description: Role
 *
 * @author hbl
 * @date 2021/03/11 0011 10:08
 */
@Data
public class Role implements Serializable
{
    private static final long serialVersionUID = -5655740993145393901L;

    private Integer id;

    private String name;

    private String memo;
}
