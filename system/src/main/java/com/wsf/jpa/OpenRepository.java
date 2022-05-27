package com.wsf.jpa;

import com.wsf.jpa.repository.EnhanceJpaRepository;
import org.springframework.data.jpa.repository.support.QuerydslJpaPredicateExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface OpenRepository<T> extends EnhanceJpaRepository<T,String> , QuerydslPredicateExecutor<T> {
}
