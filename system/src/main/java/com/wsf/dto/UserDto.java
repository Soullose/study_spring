package com.wsf.dto;

import com.google.common.collect.Sets;
import com.wsf.entity.Role;
import lombok.Data;

import java.util.Set;

/**
 * open
 * 2022/6/4
 */
@Data
public class UserDto {
    private String id;
    private String userName;
    private String password;
    private Set<Role> roles;
}
