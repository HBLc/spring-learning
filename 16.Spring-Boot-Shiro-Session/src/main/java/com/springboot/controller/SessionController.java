package com.springboot.controller;

import com.springboot.pojo.ResponseBo;
import com.springboot.pojo.UserOnline;
import com.springboot.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Description: SessionController
 *
 * @author hbl
 * @date 2021/04/30 0030 14:21
 */
@Controller
@RequestMapping("/api/v1/online")
@RequiredArgsConstructor
public class SessionController
{
    private final SessionService sessionService;

    @RequestMapping("/index")
    public String online()
    {
        return "online";
    }

    @ResponseBody
    @RequestMapping("/list")
    public List<UserOnline> list()
    {
        return sessionService.list();
    }

    @ResponseBody
    @RequestMapping("/forceLogout")
    public ResponseBo forceLogout(String id)
    {
        try
        {
            sessionService.forceLogout(id);
            return ResponseBo.ok();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseBo.error("踢出用户失败");
        }
    }
}
