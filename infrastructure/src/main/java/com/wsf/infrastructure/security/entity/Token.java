package com.wsf.infrastructure.security.entity;

import com.wsf.domain.BaseEntity;
import com.wsf.infrastructure.security.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "T_OPEN_TOKEN_")
public class Token extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -529387902106465050L;

    @Column(name = "token_",length = 512)
    private String token;

    @Column(name = "token_type_")
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    ///到期
    @Column(name = "expired_")
    private boolean expired;

    ///废除
    @Column(name = "revoked_")
    private boolean revoked;

    ///创建时间(登录时间)
    @Column(name = "create_date_time_")
    private LocalDateTime createDateTime;

    @ManyToOne
    @JoinColumn(name = "user_account_id_")
    private UserAccount userAccount;
}