package com.springboot.service;

import com.springboot.bean.Student;

import java.util.List;
import java.util.Map;

/**
 * Description: StudentService
 *
 * @author hbl
 * @date 2020/7/11 0011 15:17
 */
public interface StudentService
{
    int add(Student student);

    int update(Student student);

    int deleteBySno(String sno);

    List<Map<String, Object>> queryStudentsListMap();

    Student queryStudentBySno(String sno);
}
