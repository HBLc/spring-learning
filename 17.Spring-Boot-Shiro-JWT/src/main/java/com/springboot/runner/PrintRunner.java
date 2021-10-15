package com.springboot.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Description: PrintRunner
 *
 * @author hbl
 * @date 2021/05/27 0027 14:44
 */
@Slf4j
@Component
public class PrintRunner implements ApplicationRunner
{
    @Override
    public void run(ApplicationArguments args)
    {
        log.info("搞咩啊~");
    }
}
