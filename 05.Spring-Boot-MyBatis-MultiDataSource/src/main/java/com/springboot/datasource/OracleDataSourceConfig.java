package com.springboot.datasource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Description: OracleDataSourceConfig
 *
 * @author hbl
 * @date 2020/7/16 0016 14:27
 */
@Configuration
@MapperScan(basePackages = OracleDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "oracleSqlSessionFactory")
public class OracleDataSourceConfig
{
    // oracledao 扫描路径
    static final String PACKAGE = "com.springboot.oracledao";

    // mybatis mapper 扫描路径
    static final String MAPPER_LOCATION = "classpath:mapper/oracle/*.xml";

    @Bean(name = "oracleDataSource")
    @ConfigurationProperties("spring.datasource.druid.oracle")
    public DataSource oracleDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "oracleTransactionManager")
    public DataSourceTransactionManager oracleTransactionManager() {
        return new DataSourceTransactionManager(oracleDataSource());
    }

    @Bean(name = "oracleSqlSessionFactory")
    public SqlSessionFactory oracleSqlSessionFactory(@Qualifier("oracleDataSource") DataSource dataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        // 如果不使用 xml 的方式配置 mapper, 则可以省去下面这行 mapper location 的配置
        sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(OracleDataSourceConfig.MAPPER_LOCATION));
        return sessionFactoryBean.getObject();
    }
}
