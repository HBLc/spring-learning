package com.springboot.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Description: UserController
 *
 * @author hbl
 * @date 2021/03/11 0011 11:20
 */
@Controller
@RequestMapping("/api/v1/user")
public class UserController
{
    @RequiresPermissions("user:user")
    @GetMapping("/list")
    public String userList(Model model)
    {
        model.addAttribute("value", "获取用户信息");
        return "user";
    }

    @RequiresPermissions("user:add")
    @GetMapping("/add")
    public String userAdd(Model model)
    {
        model.addAttribute("value", "新增用户");
        return "user";
    }

    @RequiresPermissions("user:delete")
    @GetMapping("/delete")
    public String userDelete(Model model)
    {
        model.addAttribute("value", "删除用户");
        return "user";
    }
}
