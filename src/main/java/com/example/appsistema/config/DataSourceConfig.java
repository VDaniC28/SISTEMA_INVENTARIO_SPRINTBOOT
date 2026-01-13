package com.example.appsistema.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.appsistema.repository",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
public class DataSourceConfig {

    @Value("${spring.datasource.main.url}")
    private String mainDbUrl;
    @Value("${spring.datasource.main.username}")
    private String mainDbUsername;
    @Value("${spring.datasource.main.password}")
    private String mainDbPassword;

    @Value("${spring.datasource.empresa1.url}")
    private String empresa1DbUrl;
    @Value("${spring.datasource.empresa1.username}")
    private String empresa1DbUsername;
    @Value("${spring.datasource.empresa1.password}")
    private String empresa1DbPassword;

    @Bean(name = "mainDataSource")
    public DataSource mainDataSource() {
        return createDataSource(mainDbUrl, mainDbUsername, mainDbPassword);
    }

    @Bean(name = "routingDataSource")
    @Primary
    public DataSource routingDataSource() {
        TenantAwareRoutingDataSource routingDataSource = new TenantAwareRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();

        // DataSource principal (sisinventario)
        DataSource mainDS = mainDataSource();
        
        // DataSource para empresa 2 (sisinventario2)
        DataSource empresa2DataSource = createDataSource(empresa1DbUrl, empresa1DbUsername, empresa1DbPassword);
        
        // IMPORTANTE: Configurar correctamente los mappings
        targetDataSources.put("1", mainDS);           // Empresa 1 usa BD principal (sisinventario)
        targetDataSources.put("2", empresa2DataSource); // Empresa 2 usa sisinventario2
        
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(mainDS); // Default a BD principal
        
        routingDataSource.afterPropertiesSet();
        
        return routingDataSource;
    }

    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("routingDataSource") DataSource dataSource) {
        
        return builder
                .dataSource(dataSource)
                .packages("com.example.appsistema.model")
                .persistenceUnit("default")
                .build();
    }

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private DataSource createDataSource(String url, String username, String password) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        
        // Configuraciones adicionales para Hikari
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setIdleTimeout(300000);
        dataSource.setConnectionTimeout(30000);
        
        return dataSource;
    }
}