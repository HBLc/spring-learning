package com.springboot.controller;

import com.springboot.bean.Student;
import com.springboot.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: TestController
 *
 * @author hbl
 * @date 2020/6/20 0020 14:00
 */
@RestController
public class TestController
{
    @Autowired
    private StudentService studentService;

    @GetMapping("/query-student")
    public Student queryStudentBySno(String sno)
    {
        return this.studentService.queryStudentBySno(sno);
    }
}
