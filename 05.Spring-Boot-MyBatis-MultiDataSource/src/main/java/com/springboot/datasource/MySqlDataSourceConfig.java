package com.springboot.datasource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Description: MySqlDataSourceConfig
 *
 * @author hbl
 * @date 2020/7/16 0016 13:59
 */
@Configuration
@MapperScan(basePackages = MySqlDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "mySqlSqlSessionFactory")
public class MySqlDataSourceConfig
{
    // mysqldao 扫描路径
    static final String PACKAGE = "com.springboot.mysqldao";

    // mybatis mapper 扫描路径
    static final String MAPPER_LOCATION = "classpath:mapper/mysql/*.xml";

    @Primary
    @Bean(name = "mySqlDataSource")
    @ConfigurationProperties("spring.datasource.druid.mysql")
    public DataSource mySqlDataSource()
    {
        return DruidDataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "mySqlTransactionManager")
    public DataSourceTransactionManager mySqlTransactionManager()
    {
        return new DataSourceTransactionManager(mySqlDataSource());
    }

    @Primary
    @Bean(name = "mySqlSqlSessionFactory")
    public SqlSessionFactory mySqlSqlSessionFactory(@Qualifier("mySqlDataSource") DataSource dataSource) throws Exception
    {
        final SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        // 如果不使用 xml 的方式配置 mapper, 则可以省去下面这行 mapper location 的配置
        sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MySqlDataSourceConfig.MAPPER_LOCATION));
        return sessionFactoryBean.getObject();
    }
}
