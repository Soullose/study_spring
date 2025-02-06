package com.wsf.infrastructure.config;

import com.querydsl.jpa.impl.JPAProvider;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wsf.infrastructure.jpa.CurrentUserAuditorAware;
import com.wsf.jpa.repository.EnhanceJpaRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;

/**
 * open
 * SoulLose
 * 2022-05-16 14:35
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = OpenPrimaryJpaConfig.REPOSITORY_PACKAGE,
        entityManagerFactoryRef = "openEntityManagerFactory",
        transactionManagerRef = "openTransactionManager",
        repositoryBaseClass = EnhanceJpaRepositoryImpl.class)
@EnableJpaAuditing
public class OpenPrimaryJpaConfig {
    private static final Logger log = LoggerFactory.getLogger(OpenPrimaryJpaConfig.class);

    public OpenPrimaryJpaConfig() {
    }

    //	@PersistenceContext(unitName = "openDS")
//	private EntityManager entityManager;
    @Primary
    @Bean
    @ConditionalOnMissingBean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        log.debug("创建JPAQueryFactory");
        return new JPAQueryFactory(JPAProvider.getTemplates(entityManager), entityManager);
    }

    public static final String REPOSITORY_PACKAGE = "com.wsf.**";

    @Primary
    @Bean(name = "openPrimaryJpaProperties")
    @ConfigurationProperties(prefix = "spring.jpa.primary")
    public JpaProperties jpaProperties() {
        log.debug(REPOSITORY_PACKAGE);
        return new JpaProperties();
    }

    @Primary
    @Bean(name = "openEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean openEntityManagerFactory(
            @Qualifier(value = "openDataSource") DataSource openDataSource,
            @Qualifier(value = "openPrimaryJpaProperties") JpaProperties jpaProperties,
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(openDataSource)
                .properties(jpaProperties.getProperties())
                .packages(REPOSITORY_PACKAGE)
                .persistenceUnit("openDS")
                .build();
    }

    @Primary
    @Bean(name = "openTransactionManager")
    public JpaTransactionManager openTransactionManager(
            @Qualifier(value = "openEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    @Primary
    @Bean(name = "currentUserAuditorAware")
    public AuditorAware<String> currentUserAuditorAware() {
        return new CurrentUserAuditorAware();
    }
}
