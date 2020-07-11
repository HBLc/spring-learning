package com.springboot.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * Description: Student
 *
 * @author hbl
 * @date 2020/7/11 0011 15:16
 */
@Data
public class Student implements Serializable
{
    private static final long serialVersionUID = -6240769402110284535L;

    private String sno;

    private String name;

    private String sex;
}
