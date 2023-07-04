package com.wsf.jpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

@NoRepositoryBean
public interface QueryableReadRepository<T, ID> extends Repository<T, ID> {

  JPAQueryFactory getQueryFactory();

  List<T> findAll(Predicate predicate);

  List<T> findAll(Sort sort);

  List<T> findAll(Predicate predicate, Sort sort);

  List<T> findAll(OrderSpecifier<?>... orders);

  List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);

  Page<T> findAll(Pageable page);

  Page<T> findAll(Predicate predicate, Pageable page);

  Optional<T> findOne(Predicate predicate);

  boolean exists(Predicate predicate);
}