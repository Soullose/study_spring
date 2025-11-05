package com.wsf.infrastructure.security.repository;

import com.wsf.infrastructure.security.entity.Token;
import com.wsf.infrastructure.security.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
        SELECT t FROM Token t
        WHERE t.userAccount.id = :userId AND (t.expired = false OR t.revoked = false)
    """)
    List<Token> findAllValidTokensByUser(Long userId);

    Optional<Token> findByToken(String token);

    Optional<Set<Token>> findByUserAccount(UserAccount userAccount);
}