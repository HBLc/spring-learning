package com.springboot.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: TestController
 *
 * @author hbl
 * @date 2021/05/28 0028 11:33
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController
{
    /**
     * 需要登录才能访问
     */
    @GetMapping("/1")
    public String test1()
    {
        return "success";
    }

    /**
     * 需要 admin 角色才能访问
     */
    @GetMapping("2")
    @RequiresRoles("admin")
    public String test2()
    {
        return "scuuess";
    }

    /**
     * 需要 "user:add" 权限才能访问
     */
    @GetMapping("3")
    @RequiresPermissions("user:add")
    public String test3()
    {
        return "success";
    }
}
