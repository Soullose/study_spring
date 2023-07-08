package com.wsf.repository;

import com.wsf.entity.UserAccount;
import com.wsf.jpa.OpenRepository;

import java.util.Optional;

public interface UserAccountRepository extends OpenRepository<UserAccount> {

	///根据用户名查询UserAccount
	Optional<UserAccount> findByUsername(String username);
}
