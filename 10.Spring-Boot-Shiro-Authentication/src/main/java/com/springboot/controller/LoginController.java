package com.springboot.controller;

import com.springboot.pojo.ResponseBo;
import com.springboot.pojo.User;
import com.springboot.util.MD5Utils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Description: LoginController
 *
 * @author hbl
 * @date 2020/10/29 0029 14:43
 */
@Controller
public class LoginController
{
    @GetMapping("/login")
    public String login()
    {
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseBo login(String username, String password)
    {
        // 密码MD5加密
        password = MD5Utils.encrypt(username, password);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        // 获取Subject对象
        Subject subject = SecurityUtils.getSubject();
        try
        {
            subject.login(token);
            return ResponseBo.ok();
        }
        catch (UnknownAccountException | IncorrectCredentialsException | LockedAccountException e)
        {
            return ResponseBo.error(e.getMessage());
        }
        catch (AuthenticationException e)
        {
            return ResponseBo.error("认证失败~");
        }
    }

    @RequestMapping("/")
    public String redirectIndex()
    {
        return "redirect:/index";
    }

    @RequestMapping("/index")
    public String index(Model model)
    {
        // 登录成功后即可通过Subject获取登录的用户信息
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("user", user);
        return "index";
    }
}
