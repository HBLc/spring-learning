package com.springboot.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Description: User
 *
 * @author hbl
 * @date 2020/10/10 0010 10:44
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
