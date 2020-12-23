package com.springboot.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Description: User
 *
 * @author hbl
 * @date 2020/12/23 0023 15:11
 */
@Data
public class User implements Serializable
{
    private static final long serialVersionUID = -5257953647640024556L;

    private Integer id;

    private String userName;

    private String password;

    private Date createTime;

    private String status;
}
