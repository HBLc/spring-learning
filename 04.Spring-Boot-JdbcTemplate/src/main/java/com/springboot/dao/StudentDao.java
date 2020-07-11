package com.springboot.dao;

import com.springboot.bean.Student;

import java.util.List;
import java.util.Map;

/**
 * Description: StudentDao
 *
 * @author hbl
 * @date 2020/7/11 0011 15:16
 */
public interface StudentDao
{
    int add(Student student);

    int update(Student student);

    int deleteBySno(String sno);

    List<Map<String, Object>> queryStudentsListMap();

    Student queryStudentBySno(String sno);
}
