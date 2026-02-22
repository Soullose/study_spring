package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.IdCardNumber;
import com.wsf.domain.model.user.valueobject.PhoneNumber;
import com.wsf.domain.model.user.valueobject.UserName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户领域模型转换器
 * 使用MapStruct实现领域模型User与持久化实体User之间的转换
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    /**
     * 领域模型转持久化实体
     */
    @Mapping(target = "firstname", source = "name.firstName")
    @Mapping(target = "lastname", source = "name.lastName")
    @Mapping(target = "realName", source = "realName")
    @Mapping(target = "email", source = "email", qualifiedByName = "emailToValue")
    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "phoneToValue")
    @Mapping(target = "idCardNumber", source = "idCardNumber", qualifiedByName = "idCardToValue")
    @Mapping(target = "createTime", source = "createTime")
    @Mapping(target = "userAccount", ignore = true)
    com.wsf.infrastructure.persistence.entity.user.User toPO(com.wsf.domain.model.user.aggregate.User user);

    /**
     * 持久化实体转领域模型
     * 由于领域模型使用rebuild静态工厂方法，这里使用default方法实现
     */
    default com.wsf.domain.model.user.aggregate.User toDomain(com.wsf.infrastructure.persistence.entity.user.User po) {
        if (po == null) {
            return null;
        }
        
        UserName name = null;
        if (po.getFirstname() != null || po.getLastname() != null) {
            name = new UserName(
                Objects.toString(po.getFirstname(), ""),
                Objects.toString(po.getLastname(), "")
            );
        }
        
        Email email = po.getEmail() != null ? new Email(po.getEmail()) : null;
        PhoneNumber phoneNumber = po.getPhoneNumber() != null ? new PhoneNumber(po.getPhoneNumber()) : null;
        IdCardNumber idCardNumber = po.getIdCardNumber() != null ? new IdCardNumber(po.getIdCardNumber()) : null;
        
        return com.wsf.domain.model.user.aggregate.User.rebuild(
            po.getId(),
            name,
            email,
            phoneNumber,
            idCardNumber,
            po.getRealName(),
            po.getCreateTime() != null ? po.getCreateTime() : LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    /**
     * Email值对象转字符串
     */
    @Named("emailToValue")
    default String emailToValue(Email email) {
        return email != null ? email.value() : null;
    }

    /**
     * PhoneNumber值对象转字符串
     */
    @Named("phoneToValue")
    default String phoneToValue(PhoneNumber phoneNumber) {
        return phoneNumber != null ? phoneNumber.value() : null;
    }

    /**
     * IdCardNumber值对象转字符串
     */
    @Named("idCardToValue")
    default String idCardToValue(IdCardNumber idCardNumber) {
        return idCardNumber != null ? idCardNumber.value() : null;
    }
}
