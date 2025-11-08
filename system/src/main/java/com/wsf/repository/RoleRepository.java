package com.wsf.repository;

import com.wsf.domain.entity.Role;
import com.wsf.domain.entity.UserAccount;
import com.wsf.jpa.OpenRepository;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends OpenRepository<Role> {

    Optional<Set<Role>> findByUserAccounts(UserAccount userAccount);

}
