package com.springboot.dao.impl;

import com.springboot.dao.MySqlStudentDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Description: MySqlStudentDaoImpl
 *
 * @author hbl
 * @date 2020/8/12 0012 17:56
 */
@Repository
public class MySqlStudentDaoImpl implements MySqlStudentDao
{
    private final JdbcTemplate jdbcTemplate;

    public MySqlStudentDaoImpl(@Qualifier("mySqlJdbcTemplate") JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> getAllStudents()
    {
        return jdbcTemplate.queryForList("select * from student");
    }
}
