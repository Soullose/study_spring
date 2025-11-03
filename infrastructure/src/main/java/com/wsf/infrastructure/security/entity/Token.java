package com.wsf.infrastructure.security.entity;

import com.wsf.domain.BaseEntity;
import com.wsf.infrastructure.security.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Token extends BaseEntity {

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private boolean revoked;
    private boolean expired;

    ///创建时间(登录时间)
    @Column(name = "create_date_time_")
    private LocalDateTime createDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne
    @JoinColumn(name = "user_account_id_")
    private UserAccount userAccount;
}