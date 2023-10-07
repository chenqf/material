package com.maple.seata.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author 陈其丰
 */
//@Configuration
//@MapperScans({
//        @MapperScan(basePackages = {"com.maple.seata.mapper1"},sqlSessionFactoryRef = "sqlSessionFactory1"),
//        @MapperScan(basePackages = {"com.maple.seata.mapper2"},sqlSessionFactoryRef = "sqlSessionFactory2")
//})
public class DBConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.db1")
    public DataSource dataSource1(){
        return new DruidDataSource();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DataSource dataSource2(){
        return new DruidDataSource();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory1(@Qualifier("dataSource1") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory2(@Qualifier("dataSource2") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }
}
