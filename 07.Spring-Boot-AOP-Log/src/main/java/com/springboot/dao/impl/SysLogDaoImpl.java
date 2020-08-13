package com.springboot.dao.impl;

import com.springboot.dao.SysLogDao;
import com.springboot.domain.SysLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Description: SysLogDaoImpl
 *
 * @author hbl
 * @date 2020/8/13 0013 14:21
 */
@Repository("sysLogDao")
public class SysLogDaoImpl implements SysLogDao
{
    private final JdbcTemplate jdbcTemplate;

    public SysLogDaoImpl(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveSysLog(SysLog sysLog)
    {
        StringBuffer sql = new StringBuffer(" insert into sys_log ");
        sql.append(" (id,username,operation,time,method,params,ip,create_time) ");
        sql.append(" values(seq_sys_log.nextval,:username,:operation,:time,:method, ");
        sql.append(" :params,:ip,:createTime) ");

        NamedParameterJdbcTemplate npjt = new NamedParameterJdbcTemplate(this.jdbcTemplate.getDataSource());
        npjt.update(sql.toString(), new BeanPropertySqlParameterSource(sysLog));
    }
}
