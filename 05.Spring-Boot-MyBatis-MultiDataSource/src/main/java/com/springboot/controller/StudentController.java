package com.springboot.controller;

import com.springboot.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Description: StudentController
 *
 * @author hbl
 * @date 2020/7/16 0016 15:10
 */
@RestController
public class StudentController
{
    @Autowired
    private StudentService studentService;

    @RequestMapping("query-student-mysql")
    public List<Map<String, Object>> queryStudentFromMySql() {
        return studentService.getAllStudentsFromMysql();
    }

    @RequestMapping("query-student-oracle")
    public List<Map<String, Object>> queryStudentFromOracle() {
        return studentService.getAllStudentsFromOracle();
    }
}
