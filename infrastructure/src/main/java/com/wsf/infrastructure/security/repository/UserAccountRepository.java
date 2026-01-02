package com.wsf.infrastructure.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.wsf.domain.model.entity.UserAccount;

@Repository
public interface UserAccountRepository
    extends
      JpaRepository<UserAccount, String>,
      JpaSpecificationExecutor<UserAccount>,
      QuerydslPredicateExecutor<UserAccount> {

  /// 根据用户名查询UserAccount
  Optional<UserAccount> findByUsername(String username);
}