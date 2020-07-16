package com.springboot.service.impl;

import com.springboot.mysqldao.MySqlDaoMapper;
import com.springboot.oracledao.OracleDaoMapper;
import com.springboot.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Description: StudentServiceImpl
 *
 * @author hbl
 * @date 2020/7/16 0016 15:19
 */
@Service("studentService")
public class StudentServiceImpl implements StudentService
{
    @Autowired
    private MySqlDaoMapper mySqlDaoMapper;

    @Autowired
    private OracleDaoMapper oracleDaoMapper;

    @Override
    public List<Map<String, Object>> getAllStudentsFromMysql()
    {
        return mySqlDaoMapper.getAllStudents();
    }

    @Override
    public List<Map<String, Object>> getAllStudentsFromOracle()
    {
        return oracleDaoMapper.getAllStudents();
    }
}
