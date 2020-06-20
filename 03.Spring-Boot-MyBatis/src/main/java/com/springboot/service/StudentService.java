package com.springboot.service;

import com.springboot.bean.Student;

/**
 * Description: StudentService
 *
 * @author hbl
 * @date 2020/6/5 0005 15:30
 */
public interface StudentService
{
    // int add(Student student);
    //
    // int update(Student student);
    //
    // int deleteByIds(String sno);

    Student queryStudentBySno(String sno);
}
