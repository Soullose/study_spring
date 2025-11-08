package com.wsf.mapstruct;

import com.wsf.domain.BaseMapper;
import com.wsf.dto.UserDto;
import com.wsf.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",uses = {RoleMapper.class},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends BaseMapper<UserDto, User> {
}
