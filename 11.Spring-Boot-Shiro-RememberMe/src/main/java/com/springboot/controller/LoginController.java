package com.springboot.controller;

import com.springboot.pojo.ResponseBo;
import com.springboot.pojo.User;
import com.springboot.utils.MD5Utils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Description: LoginController
 *
 * @author hbl
 * @date 2020/12/23 0023 15:42
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
    public ResponseBo login(String username, String password, Boolean rememberMe)
    {
        password = MD5Utils.encrypt(username, password);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
        Subject subject = SecurityUtils.getSubject();
        try
        {
            subject.login(token);
            return ResponseBo.ok();
        } catch (UnknownAccountException | LockedAccountException | IncorrectCredentialsException e)
        {
            return ResponseBo.error(e.getMessage());
        }
        catch (AuthenticationException e)
        {
            return ResponseBo.error("认证失败~");
        }
    }

    @GetMapping("/")
    public String redirectIndex()
    {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index(Model model)
    {
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("user", user);
        return "index";
    }
}
