package com.wsf.dto;

import com.wsf.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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
