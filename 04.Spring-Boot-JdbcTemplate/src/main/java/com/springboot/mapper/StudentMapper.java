package com.springboot.mapper;

import com.springboot.bean.Student;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Description: StudentMapper
 *
 * @author hbl
 * @date 2020/7/11 0011 15:31
 */
public class StudentMapper implements RowMapper<Student>
{
    @Override
    public Student mapRow(ResultSet resultSet, int i) throws SQLException
    {
        Student student = new Student();
        student.setSno(resultSet.getString("sno"));
        student.setName(resultSet.getString("name"));
        student.setSex(resultSet.getString("sex"));
        return student;
    }
}
