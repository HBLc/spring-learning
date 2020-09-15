package com.springboot.service.impl;

import com.springboot.bean.Student;
import com.springboot.mapper.StudentMapper;
import com.springboot.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description: StudentServiceImpl
 *
 * @author hbl
 * @date 2020/6/5 0005 15:30
 */
@Service("studentService")
public class StudentServiceImpl implements StudentService
{
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public Student update(Student student)
    {
        this.studentMapper.update(student);
        return this.studentMapper.queryStudentBySno(student.getSno());
    }

    @Override
    public int deleteBySno(String sno)
    {
        return this.studentMapper.deleteBySno(sno);
    }

    @Override
    public Student queryStudentBySno(String sno)
    {
        return this.studentMapper.queryStudentBySno(sno);
    }
}
