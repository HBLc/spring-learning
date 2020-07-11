package com.springboot.service.impl;

import com.springboot.bean.Student;
import com.springboot.dao.StudentDao;
import com.springboot.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Description: StudentServiceImpl
 *
 * @author hbl
 * @date 2020/7/11 0011 15:17
 */
@Service("studentService")
public class StudentServiceImpl implements StudentService
{
    private final StudentDao studentDao;

    public StudentServiceImpl(StudentDao studentDao)
    {
        this.studentDao = studentDao;
    }

    @Override
    public int add(Student student)
    {
        return this.studentDao.add(student);
    }

    @Override
    public int update(Student student)
    {
        return this.studentDao.update(student);
    }

    @Override
    public int deleteBySno(String sno)
    {
        return this.studentDao.deleteBySno(sno);
    }

    @Override
    public List<Map<String, Object>> queryStudentsListMap()
    {
        return this.studentDao.queryStudentsListMap();
    }

    @Override
    public Student queryStudentBySno(String sno)
    {
        return this.studentDao.queryStudentBySno(sno);
    }
}
