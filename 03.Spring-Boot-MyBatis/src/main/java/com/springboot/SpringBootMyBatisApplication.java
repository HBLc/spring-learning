package com.springboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.springboot.mapper")
public class SpringBootMyBatisApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(SpringBootMyBatisApplication.class, args);
    }

}
