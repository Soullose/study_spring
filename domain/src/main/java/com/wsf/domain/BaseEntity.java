package com.wsf.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.wsf.domain.annotation.BaseId;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * open
 * SoulLose
 * 2022-05-04 16:42
 */
@Getter
@Setter
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

  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (id == null || object == null || getClass() != object.getClass()) {
      return false;
    }

    final BaseEntity other = (BaseEntity) object;

    return id.equals(other.id);
  }

  public int hashCode() {
    return (id == null) ? super.hashCode() : id.hashCode();
  }

}