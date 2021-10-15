package com.springboot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * Description: User
 *
 * @author hbl
 * @date 2021/05/11 0011 16:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable
{
    private static final long serialVersionUID = -6272237040409002406L;

    private String username;

    private String password;

    private Set<String> role;

    private Set<String> permission;
}
