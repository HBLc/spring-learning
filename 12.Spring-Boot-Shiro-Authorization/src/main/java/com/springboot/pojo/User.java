package com.springboot.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Description: User
 *
 * @author hbl
 * @date 2021/03/11 0011 10:11
 */
@Data
public class User implements Serializable
{
    private static final long serialVersionUID = -5440372534300871944L;

    private Integer id;

    private String userName;

    private String password;

    private Date createTime;

    private String status;
}
