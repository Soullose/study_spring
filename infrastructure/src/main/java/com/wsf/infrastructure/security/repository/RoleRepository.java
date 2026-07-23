package com.wsf.infrastructure.security.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.wsf.infrastructure.persistence.entity.role.Role;
import com.wsf.infrastructure.persistence.entity.user.UserAccountPO;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {

    Optional<Set<Role>> findByUserAccounts(UserAccountPO userAccount);
}