package com.wsf.infrastructure.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * open
 * SoulLose
 * 2022-05-16 14:26
 */
@Configuration
public class OpenPrimaryDataSource {
    
    @Primary
    @Bean(name = "openDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.open")
    public DataSourceProperties openDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Primary
    @Bean(name = "openDataSource")
    public DataSource openDataSource() {
        return this.openDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
