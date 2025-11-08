package com.wsf.infrastructure.security.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.wsf.domain.entity.Role;
import com.wsf.domain.entity.UserAccount;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {

    Optional<Set<Role>> findByUserAccounts(UserAccount userAccount);
}