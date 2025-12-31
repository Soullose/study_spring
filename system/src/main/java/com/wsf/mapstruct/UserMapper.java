package com.wsf.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.wsf.domain.BaseMapper;
import com.wsf.domain.model.entity.User;
import com.wsf.dto.UserDto;

@Mapper(componentModel = "spring",uses = {RoleMapper.class},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends BaseMapper<UserDto, User> {
}
