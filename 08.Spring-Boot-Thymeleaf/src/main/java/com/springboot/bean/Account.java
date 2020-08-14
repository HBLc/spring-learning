package com.springboot.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: Account
 *
 * @author hbl
 * @date 2020/8/14 0014 16:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account
{
    private String account;
    private String name;
    private String password;
    private String accountType;
    private String tel;
}
