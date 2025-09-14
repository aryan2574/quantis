package com.quantis.postgres_writer_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration for the Postgres Writer Service.
 * 
 * This service uses PostgreSQL with JPA/Hibernate for:
 * 1. ACID compliance for financial data
 * 2. High-performance batch processing
 * 3. Connection pooling with HikariCP
 * 4. Transaction management
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.quantis.postgres_writer_service.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String datasourceUsername;
    
    @Value("${spring.datasource.password}")
    private String datasourcePassword;
    
    @Value("${spring.datasource.driver-class-name}")
    private String datasourceDriverClassName;
    
    /**
     * Entity manager factory with optimized Hibernate settings
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.quantis.postgres_writer_service.model");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(false); // Disable in production
        vendorAdapter.setGenerateDdl(false); // Use Flyway or manual schema management
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        
        // Hibernate performance optimizations
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop"); // Create tables automatically
        
        // Batch processing optimizations
        properties.setProperty("hibernate.jdbc.batch_size", "50");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.batch_versioned_data", "true");
        
        // Connection pool optimizations
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        // Caching (use second-level cache in production)
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        
        // Performance monitoring
        properties.setProperty("hibernate.generate_statistics", "false");
        properties.setProperty("hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS", "1000");
        
        // Connection validation
        properties.setProperty("hibernate.connection.isolation", "2"); // READ_COMMITTED
        
        em.setJpaProperties(properties);
        
        return em;
    }
    
    /**
     * Transaction manager for JPA operations
     */
    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
