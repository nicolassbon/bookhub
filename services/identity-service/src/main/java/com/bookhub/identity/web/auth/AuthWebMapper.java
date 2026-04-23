package com.bookhub.identity.web.auth;

import com.bookhub.identity.application.auth.LoginUserCommand;
import com.bookhub.identity.application.auth.LoginUserResult;
import com.bookhub.identity.application.auth.RefreshSessionResult;
import com.bookhub.identity.application.auth.RegisterUserCommand;
import com.bookhub.identity.application.auth.RegisterUserResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthWebMapper {

  RegisterUserCommand toRegisterUserCommand(RegisterRequest request);

  RegisterResponse toRegisterResponse(RegisterUserResult result);

  LoginUserCommand toLoginUserCommand(LoginRequest request);

  LoginResponse toLoginResponse(LoginUserResult result);

  LoginResponse toLoginResponse(RefreshSessionResult result);

  LoginResponse.LoginUserResponse toLoginUserResponse(LoginUserResult.LoginUserView user);
}
