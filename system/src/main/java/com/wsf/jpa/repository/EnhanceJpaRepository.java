package com.wsf.jpa.repository;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import com.querydsl.jpa.impl.JPAQueryFactory;

@NoRepositoryBean
public interface EnhanceJpaRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    EntityManager getEntityManager();

    JPAQueryFactory getQueryFactory();

    <M, N> M getReference(Class<M> clazz, N id);
}
