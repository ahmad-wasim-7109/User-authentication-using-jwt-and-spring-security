package com.user.auth.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties
public class DataSourceConfig {


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource customDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/user_auth_db");
        return dataSource;
    }
}