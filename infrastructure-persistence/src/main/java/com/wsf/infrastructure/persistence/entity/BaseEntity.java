package com.wsf.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.wsf.infrastructure.jpa.id.annotation.BaseId;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

/**
 * open
 * SoulLose
 * 2022-05-04 16:42
 */
@Data
@MappedSuperclass
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "custom-id-generator")
  @BaseId
  @Column(name = "id_")
  @Access(AccessType.PROPERTY)
  private String id;

  @CreatedDate // 自动填充创建时间
  @Column(name = "create_date_", updatable = false, columnDefinition = "timestamp")
  private LocalDateTime createDate;// 创建时间

  @LastModifiedDate // 自动填充修改时间
  @Column(name = "modify_date_", columnDefinition = "timestamp")
  private LocalDateTime modifyDate;// 修改时间

}