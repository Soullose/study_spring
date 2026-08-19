package com.wsf.infrastructure.jpa.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface OpenRepository<T> extends EnhanceJpaRepository<T, String>, QuerydslPredicateExecutor<T> {
}
