package com.wsf.repository;

import java.util.Optional;
import java.util.Set;

import com.wsf.infrastructure.jpa.repository.OpenRepository;
import com.wsf.infrastructure.persistence.entity.role.Role;
import com.wsf.infrastructure.persistence.entity.user.UserAccount;

public interface RoleRepository extends OpenRepository<Role> {

    Optional<Set<Role>> findByUserAccounts(UserAccount userAccount);

}
