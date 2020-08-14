package com.springboot.controller;

import com.springboot.bean.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: IndexController
 *
 * @author hbl
 * @date 2020/8/14 0014 16:38
 */
@Controller
@RequestMapping("/api/v1/thymeleaf")
public class IndexController
{
    @GetMapping("/account")
    public String index(Model m)
    {
        List<Account> list = new ArrayList<>();
        list.add(new Account("账户A", "AAA", "password_A", "admin", "123456789"));
        list.add(new Account("账户B", "BBB", "password_B", "user", "987654321"));
        m.addAttribute("accountList", list);
        return "account";
    }
}
