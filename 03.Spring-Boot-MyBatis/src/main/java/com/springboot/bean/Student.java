package com.springboot.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * Description: Student
 *
 * @author hbl
 * @date 2020/6/4 0004 16:32
 */
@Data
public class Student implements Serializable
{
    private static final long serialVersionUID = -5871112691729687726L;

    private String sno;

    private String name;

    private String sex;
}
