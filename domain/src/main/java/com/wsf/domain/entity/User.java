package com.wsf.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wsf.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_OPEN_USER_")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User extends BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 3050292059722106684L;

	@Column(name = "first_name_")
	private String firstname;

	@Column(name = "last_name_")
	private String lastname;

	@Column(name = "real_name_")
	private String realName;

	@Column(name = "id_card_number_")
	private String idCardNumber;

	@Column(name = "phone_number_")
	private String phoneNumber;

	@Column(name = "email_")
	private String email;

	@Column(name = "create_time_")
	private LocalDateTime createTime;

	@OneToOne()
	@JoinColumn(name = "useraccount_id_")
	private UserAccount userAccount;
}