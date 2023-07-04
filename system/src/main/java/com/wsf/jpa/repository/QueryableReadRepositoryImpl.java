package com.wsf.jpa.repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslJpaPredicateExecutor;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * open
 * SoulLose
 * 2022-05-25 14:43
 */
@Transactional
public class QueryableReadRepositoryImpl<T,ID> extends QuerydslJpaPredicateExecutor<T> implements QueryableReadRepository<T,ID>{
    
    private static final EntityPathResolver resolver = SimpleEntityPathResolver.INSTANCE;
    
    private final EntityPath<T> path;
    private final PathBuilder<T> builder;
    private final Querydsl querydsl;
    private final JPAQueryFactory queryFactory;
    
    public QueryableReadRepositoryImpl(JpaEntityInformation<T, ?> entityInformation,
                                       EntityManager entityManager,
                                       EntityPathResolver resolver,
                                       CrudMethodMetadata metadata) {
        super(entityInformation, entityManager, resolver, metadata);
        this.path = resolver.createPath(entityInformation.getJavaType());
        this.builder = new PathBuilder<T>(path.getType(), path.getMetadata());
        this.querydsl = new Querydsl(entityManager, builder);
        this.queryFactory = new JPAQueryFactory(entityManager);
    }
    
    
    @Override
    public JPAQueryFactory getQueryFactory() {
        return this.queryFactory;
    }
    
    @Override
    public Optional<T> findOne(Predicate predicate) {
        return super.findOne(predicate);
    }
    
    @Override
    public List<T> findAll(OrderSpecifier<?>... orders) {
        return super.findAll(orders);
    }
    
    @Override
    public List<T> findAll(Predicate predicate, Sort sort) {
        return executeSorted(createQuery(predicate).select(path), sort);
    }
    
    @Override
    public Page<T> findAll(Predicate predicate, Pageable pageable) {
        return super.findAll(predicate, pageable);
    }
    

    
    @Override
    public List<T> findAll(Predicate predicate) {
        return super.findAll(predicate);
    }
    
    public List<T> findAll(Sort sort) {
        return executeSorted(createQuery().select(path), sort);
    }
    
    @Override
    public Page<T> findAll(Pageable pageable) {
        final JPQLQuery<?> countQuery = createCountQuery();
        JPQLQuery<T> query = querydsl.applyPagination(pageable, createQuery().select(path));
        
        return PageableExecutionUtils.getPage(
                query.distinct().fetch(),
                pageable,
                countQuery::fetchCount);
    }
    
    private List<T> executeSorted(JPQLQuery<T> query, Sort sort) {
        return querydsl.applySorting(sort, query).distinct().fetch();
    }
}
