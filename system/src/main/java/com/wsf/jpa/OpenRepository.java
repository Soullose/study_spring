package com.wsf.jpa;

import com.wsf.jpa.repository.EnhanceJpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface OpenRepository<T> extends EnhanceJpaRepository<T,String> , QuerydslPredicateExecutor<T> {
}
