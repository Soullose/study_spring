package com.wsf.infrastructure.persistence.entity.user;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wsf.infrastructure.persistence.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_OPEN_USER_")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Comment("系统用户表")
@EntityListeners(AuditingEntityListener.class)
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
  @Comment("邮箱")
  private String email;

  @Column(name = "create_time_")
  private LocalDateTime createTime;

  @OneToOne()
  @JoinColumn(name = "useraccount_id_")
  private UserAccount userAccount;
}