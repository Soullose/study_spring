package com.wsf.infrastructure.persistence.entity.token;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.wsf.infrastructure.persistence.entity.BaseEntity;
import com.wsf.infrastructure.persistence.entity.token.enums.TokenType;
import com.wsf.infrastructure.persistence.entity.user.UserAccount;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "T_OPEN_TOKEN_")
@Comment("登录认证记录表")
public class Token extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -529387902106465050L;

    @Column(length = 512)
    @Comment("认证号")
    private String token;

//    @Column(name = "token_type_")
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    ///到期
//    @Column(name = "expired_")
    private boolean expired;

    ///废除
//    @Column(name = "revoked_")
    private boolean revoked;

    ///创建时间(登录时间)
//    @Column(name = "create_date_time_")
    private LocalDateTime createDateTime;

    @ManyToOne
    @JoinColumn(name = "user_account_id_")
    private UserAccount userAccount;
}