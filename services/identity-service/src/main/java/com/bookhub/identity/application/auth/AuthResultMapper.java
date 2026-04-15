package com.bookhub.identity.application.auth;

import com.bookhub.identity.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthResultMapper {

    @Mapping(target = "userId", expression = "java(user.getId().toString())")
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    RegisterUserResult toRegisterUserResult(User user);

    @Mapping(target = "userId", expression = "java(user.getId().toString())")
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    LoginUserResult.LoginUserView toLoginUserView(User user);
}
