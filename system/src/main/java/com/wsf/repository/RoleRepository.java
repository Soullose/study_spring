package com.wsf.repository;

import com.wsf.entity.Role;
import com.wsf.infrastructure.security.entity.UserAccount;
import com.wsf.jpa.OpenRepository;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends OpenRepository<Role> {

    Optional<Set<Role>> findByUserAccounts(UserAccount userAccount);

}
