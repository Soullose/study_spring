package com.wsf.queryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

/**
 * open
 * SoulLose
 * 2022-05-11 21:30
 */
@Configuration
public class QueryDslJpaConfiguration {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
