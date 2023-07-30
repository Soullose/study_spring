package com.wsf.entity;

import com.wsf.domain.BaseEntity;
import com.wsf.enums.TokenType;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "t_open_token_")
public class Token extends BaseEntity implements Serializable {

	private static final long serialVersionUID = -529387902106465050L;

	@Column(name = "token_")
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

	@ManyToOne
	@JoinColumn(name = "user_account_id_")
	private UserAccount userAccount;

	@CreatedBy
	@Column(name = "create_user_")
	private String createdBy;

}
