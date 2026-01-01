package com.wsf.infrastructure.jpa.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
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

import com.querydsl.jpa.impl.JPAProvider;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wsf.infrastructure.jpa.audit.CurrentUserAuditorAware;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * open SoulLose 2022-05-16 14:35
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = OpenPrimaryJpaConfig.REPOSITORY_PACKAGE, entityManagerFactoryRef = "openEntityManagerFactory", transactionManagerRef = "openTransactionManager"
)
@EnableJpaAuditing
@EntityScan(basePackages = OpenPrimaryJpaConfig.DOMAIN_PACKAGE)
public class OpenPrimaryJpaConfig {
  private static final Logger log = LoggerFactory.getLogger(OpenPrimaryJpaConfig.class);

  public static final String REPOSITORY_PACKAGE = "com.wsf.infrastructure.**";
  public static final String DOMAIN_PACKAGE = "com.wsf.domain.**";

  public OpenPrimaryJpaConfig() {
  }

  @Primary
  @Bean(name = "jpaQueryFactory")
  @ConditionalOnMissingBean
  public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
    log.debug("创建JPAQueryFactory");
    return new JPAQueryFactory(JPAProvider.getTemplates(entityManager), entityManager);
  }

  @Primary
  @Bean(name = "openPrimaryJpaProperties")
  @ConfigurationProperties(prefix = "spring.jpa.primary")
  public JpaProperties openPrimaryJpaProperties() {
    log.debug(REPOSITORY_PACKAGE);
    return new JpaProperties();
  }

  @Primary
  @Bean(name = "openPrimaryHibernateProperties")
  @ConfigurationProperties(prefix = "spring.jpa.hibernate")
  public HibernateProperties openPrimaryHibernateProperties() {
    HibernateProperties hibernateProperties = new HibernateProperties();
    hibernateProperties.setDdlAuto("update");
    hibernateProperties.getNaming().setImplicitStrategy("org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
    /// PhysicalNamingStrategyStandardImpl
//    hibernateProperties.getNaming().setPhysicalStrategy("org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
    hibernateProperties.getNaming().setPhysicalStrategy("org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
    return hibernateProperties;
  }

  @Primary
  @Bean(name = "openEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean openEntityManagerFactory(
      @Qualifier(value = "openDataSource") DataSource openDataSource,
      @Qualifier(value = "openPrimaryJpaProperties") JpaProperties jpaProperties,
      @Qualifier(value = "openPrimaryHibernateProperties") HibernateProperties hibernateProperties,
      EntityManagerFactoryBuilder builder) {
    log.debug("创建Primary EntityManagerFactory，数据源: {}", openDataSource);
    /// 合并 JPA 和 Hibernate 属性
    Map<String, Object> properties = new HashMap<>();
    properties.putAll(jpaProperties.getProperties());
    properties.putAll(hibernateProperties.determineHibernateProperties(
            jpaProperties.getProperties(),
            new HibernateSettings()
    ));
    return builder.dataSource(openDataSource).properties(properties)
        .packages(REPOSITORY_PACKAGE, DOMAIN_PACKAGE)
        .persistenceUnit("openDS").build();
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
