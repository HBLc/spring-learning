package com.springboot.controller;

import com.springboot.bean.XiaoHongBean;
import com.springboot.bean.XiaoMingBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: IndexController
 *
 * @author hbl
 * @date 2020/6/4 0004 10:25
 */
@RestController
public class IndexController
{
    private final XiaoMingBean xiaoMingBean;
    private final XiaoHongBean xiaoHongBean;

    public IndexController(XiaoMingBean xiaoMingBean, XiaoHongBean xiaoHongBean)
    {
        this.xiaoMingBean = xiaoMingBean;
        this.xiaoHongBean = xiaoHongBean;
    }

    @GetMapping("/xiao-ming")
    public String xiaoMing()
    {
        return xiaoMingBean.toString();
    }

    @GetMapping("/xiao-hong")
    public String xiaoHong()
    {
        return xiaoHongBean.toString();
    }
}
