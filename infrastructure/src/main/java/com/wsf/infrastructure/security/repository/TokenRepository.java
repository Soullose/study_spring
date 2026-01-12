package com.wsf.infrastructure.security.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wsf.infrastructure.persistence.entity.token.Token;
import com.wsf.infrastructure.persistence.entity.user.UserAccount;

public interface TokenRepository extends JpaRepository<Token, String> {

    @Query("""
        SELECT t FROM Token t
        WHERE t.userAccount.id = :userId AND (t.expired = false OR t.revoked = false)
    """)
    List<Token> findAllValidTokensByUser(String userId);

    Optional<Token> findByToken(String token);

    Optional<Set<Token>> findByUserAccount(UserAccount userAccount);
}