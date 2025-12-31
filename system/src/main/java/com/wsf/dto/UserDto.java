package com.wsf.dto;

import java.util.Set;

import com.wsf.domain.model.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * open
 * 2022/6/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String userName;
    private String password;
    private Set<Role> roles;
}
