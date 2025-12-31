package com.wsf.domain.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EnhanceJpaRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
//    EntityManager getEntityManager();
//
//    JPAQueryFactory getQueryFactory();

    <M, N> M getReference(Class<M> clazz, N id);
}
