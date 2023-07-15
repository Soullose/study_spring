package com.wsf.repository;

import com.wsf.entity.Token;
import com.wsf.entity.UserAccount;
import com.wsf.jpa.OpenRepository;

import java.util.Optional;
import java.util.Set;

public interface TokenRepository extends OpenRepository<Token>{

	Optional<Set<Token>> findByUserAccount(UserAccount userAccount);

	Optional<Token> findByToken(String token);
}
