package com.wsf.repository;

import java.util.Optional;
import java.util.Set;

import com.wsf.domain.model.entity.Role;
import com.wsf.domain.model.entity.UserAccount;
import com.wsf.infrastructure.jpa.repository.OpenRepository;

public interface RoleRepository extends OpenRepository<Role> {

    Optional<Set<Role>> findByUserAccounts(UserAccount userAccount);

}
