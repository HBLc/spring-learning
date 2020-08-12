package com.springboot.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Description: DataSourceConfig
 *
 * @author hbl
 * @date 2020/8/12 0012 15:59
 */
@Configuration
public class DataSourceConfig
{
    @Primary
    @Bean(name = "mySqlDataSource")
    @ConfigurationProperties("spring.datasource.druid.mysql")
    public DataSource dataSourceOne()
    {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "oracleDataSource")
    @ConfigurationProperties("spring.datasource.druid.oracle")
    public DataSource dataSourceTwo()
    {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean("mySqlJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("mySqlDataSource") DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }

    @Bean("oracleJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate(@Qualifier("oracleDataSource") DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }
}
