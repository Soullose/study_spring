package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.account.valueobject.AccountStatus;
import com.wsf.domain.model.account.valueobject.Password;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * 用户账户领域模型转换器
 * 使用MapStruct实现领域模型UserAccount与持久化实体UserAccount之间的转换
 */
@Mapper(componentModel = "spring")
public interface UserAccountConverter {

    UserAccountConverter INSTANCE = Mappers.getMapper(UserAccountConverter.class);

    /**
     * 领域模型转持久化实体
     */
    @Mapping(target = "password", source = "password", qualifiedByName = "passwordToValue")
    @Mapping(target = "enabled", source = "status", qualifiedByName = "statusToEnabled")
    @Mapping(target = "accountNonExpired", source = "status", qualifiedByName = "statusToNonExpired")
    @Mapping(target = "accountNonLocked", source = "status", qualifiedByName = "statusToNonLocked")
    @Mapping(target = "credentialsNonExpired", source = "status", qualifiedByName = "statusToCredentialsNonExpired")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "roles", ignore = true)
    com.wsf.infrastructure.persistence.entity.user.UserAccount toPO(com.wsf.domain.model.account.aggregate.UserAccount account);

    /**
     * 持久化实体转领域模型
     * 由于领域模型使用rebuild静态工厂方法，这里使用default方法实现
     */
    default com.wsf.domain.model.account.aggregate.UserAccount toDomain(com.wsf.infrastructure.persistence.entity.user.UserAccount po) {
        if (po == null) {
            return null;
        }
        
        Password password = po.getPassword() != null ? new Password(po.getPassword(), true) : null;
        
        AccountStatus status = new AccountStatus(
            po.isEnabled(),
            po.isAccountNonExpired(),
            po.isAccountNonLocked(),
            po.isCredentialsNonExpired()
        );
        
        String userId = po.getUser() != null ? po.getUser().getId() : null;
        
        return com.wsf.domain.model.account.aggregate.UserAccount.rebuild(
            po.getId(),
            po.getUsername(),
            password,
            status,
            userId,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    /**
     * Password值对象转字符串
     */
    @Named("passwordToValue")
    default String passwordToValue(Password password) {
        return password != null ? password.value() : null;
    }

    /**
     * AccountStatus转enabled
     */
    @Named("statusToEnabled")
    default boolean statusToEnabled(AccountStatus status) {
        return status != null && status.enabled();
    }

    /**
     * AccountStatus转accountNonExpired
     */
    @Named("statusToNonExpired")
    default boolean statusToNonExpired(AccountStatus status) {
        return status != null && status.accountNonExpired();
    }

    /**
     * AccountStatus转accountNonLocked
     */
    @Named("statusToNonLocked")
    default boolean statusToNonLocked(AccountStatus status) {
        return status != null && status.accountNonLocked();
    }

    /**
     * AccountStatus转credentialsNonExpired
     */
    @Named("statusToCredentialsNonExpired")
    default boolean statusToCredentialsNonExpired(AccountStatus status) {
        return status != null && status.credentialsNonExpired();
    }
}
