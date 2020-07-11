package com.springboot.controller;

import com.springboot.bean.Student;
import com.springboot.service.StudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Description: TestController
 *
 * @author hbl
 * @date 2020/7/11 0011 15:16
 */
@RestController
@RequestMapping("/api/v1/student")
public class TestController
{
    private final StudentService studentService;

    public TestController(StudentService studentService)
    {
        this.studentService = studentService;
    }

    @GetMapping("/query")
    public Student queryStudentBySno(@RequestParam String sno)
    {
        return this.studentService.queryStudentBySno(sno);
    }

    @GetMapping
    public List<Map<String, Object>> queryStudentAll()
    {
        return this.studentService.queryStudentsListMap();
    }

    @GetMapping("/add")
    public int saveStudent(@RequestParam String sno, @RequestParam String name, @RequestParam String sex)
    {
        Student student = new Student();
        student.setSno(sno);
        student.setName(name);
        student.setSex(sex);
        return this.studentService.add(student);
    }

    @GetMapping("/delete")
    public int deleteStudentBySno(@RequestParam String sno)
    {
        return this.studentService.deleteBySno(sno);
    }
}
