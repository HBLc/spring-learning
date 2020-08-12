package com.springboot.controller;

import com.springboot.service.StudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Description: StudentController
 *
 * @author hbl
 * @date 2020/8/12 0012 18:08
 */
@RestController
@RequestMapping("/api/v1/student")
public class StudentController
{
    private final StudentService studentService;

    public StudentController(StudentService studentService)
    {
        this.studentService = studentService;
    }

    @GetMapping("/mysql")
    public List<Map<String, Object>> queryStudentsFromMySQL()
    {
        return studentService.getAllStudentsFromMySQL();
    }

    @GetMapping("/oracle")
    public List<Map<String, Object>> queryStudentsFromOracle()
    {
        return studentService.getAllStudentsFromOracle();
    }
}
