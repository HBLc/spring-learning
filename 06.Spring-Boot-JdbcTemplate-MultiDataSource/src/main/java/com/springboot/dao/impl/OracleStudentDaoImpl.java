package com.springboot.dao.impl;

import com.springboot.dao.OracleStudentDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Description: OracleStudentDaoImpl
 *
 * @author hbl
 * @date 2020/8/12 0012 18:02
 */
@Repository
public class OracleStudentDaoImpl implements OracleStudentDao
{
    private final JdbcTemplate jdbcTemplate;

    public OracleStudentDaoImpl(@Qualifier("oracleJdbcTemplate") JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> getAllStudents()
    {
        return jdbcTemplate.queryForList("select * from student");
    }
}
