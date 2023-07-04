package com.wsf.jpa;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import com.wsf.jpa.repository.EnhanceJpaRepository;

@NoRepositoryBean
public interface OpenRepository<T> extends EnhanceJpaRepository<T, String>, QuerydslPredicateExecutor<T> {
}
