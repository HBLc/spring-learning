package com.springboot.service.impl;

import com.springboot.dao.MySqlStudentDao;
import com.springboot.dao.OracleStudentDao;
import com.springboot.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Description: StudentServiceImpl
 *
 * @author hbl
 * @date 2020/8/12 0012 18:06
 */
@Service("studentService")
public class StudentServiceImpl implements StudentService
{
    private final MySqlStudentDao mySqlStudentDao;
    private final OracleStudentDao oracleStudentDao;

    public StudentServiceImpl(MySqlStudentDao mySqlStudentDao, OracleStudentDao oracleStudentDao)
    {
        this.mySqlStudentDao = mySqlStudentDao;
        this.oracleStudentDao = oracleStudentDao;
    }

    @Override
    public List<Map<String, Object>> getAllStudentsFromMySQL()
    {
        return mySqlStudentDao.getAllStudents();
    }

    @Override
    public List<Map<String, Object>> getAllStudentsFromOracle()
    {
        return oracleStudentDao.getAllStudents();
    }
}
