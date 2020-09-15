package com.springboot;

import com.springboot.bean.Student;
import com.springboot.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = SpringBootRedisCacheApplication.class)
class SpringBootRedisCacheApplicationTests
{
    @Resource
    private StudentService service;

    @Test
    void test1()
    {
        Student student1 = this.service.queryStudentBySno("001");
        System.out.println("学号为" + student1.getSno() + "的学生姓名姓名: " + student1.getName());

        Student student2 = this.service.queryStudentBySno("001");
        System.out.println("学号为" + student2.getSno() + "的学生姓名姓名: " + student2.getName());
    }

    @Test
    void test2()
    {
        Student student1 = this.service.queryStudentBySno("001");
        System.out.println("学号为" + student1.getSno() + "的学生姓名姓名: " + student1.getName());

        student1.setName("小明");
        this.service.update(student1);

        Student student2 = this.service.queryStudentBySno("001");
        System.out.println("学号为" + student2.getSno() + "的学生姓名姓名: " + student2.getName());
    }

}
