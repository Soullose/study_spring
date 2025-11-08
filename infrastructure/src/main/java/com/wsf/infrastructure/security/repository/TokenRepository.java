package com.wsf.infrastructure.security.repository;

import com.wsf.domain.entity.Token;
import com.wsf.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TokenRepository extends JpaRepository<Token, String> {

    @Query("""
        SELECT t FROM Token t
        WHERE t.userAccount.id = :userId AND (t.expired = false OR t.revoked = false)
    """)
    List<Token> findAllValidTokensByUser(String userId);

    Optional<Token> findByToken(String token);

    Optional<Set<Token>> findByUserAccount(UserAccount userAccount);
}