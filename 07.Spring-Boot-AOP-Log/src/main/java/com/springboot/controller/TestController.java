package com.springboot.controller;

import com.springboot.annotation.Log;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: TestController
 *
 * @author hbl
 * @date 2020/8/13 0013 15:05
 */
@RestController
@RequestMapping("/api/v1/aop")
public class TestController
{
    @Log("执行方法一")
    @GetMapping("/one")
    public void methodOne(@RequestParam String name) { }

    @Log("执行方法二")
    @GetMapping("/two")
    public void methodTwo() throws InterruptedException
    {
        Thread.sleep(2000);
    }

    @Log("执行方法三")
    @GetMapping("/three")
    public void methodThree(@RequestParam String name, @RequestParam String age) { }
}
