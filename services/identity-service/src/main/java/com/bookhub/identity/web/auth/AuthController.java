package com.bookhub.identity.web.auth;

import com.bookhub.identity.application.auth.LoginUserCommand;
import com.bookhub.identity.application.auth.LoginUserResult;
import com.bookhub.identity.application.auth.LoginUserService;
import com.bookhub.identity.application.auth.RegisterUserCommand;
import com.bookhub.identity.application.auth.RegisterUserResult;
import com.bookhub.identity.application.auth.RegisterUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserService registerUserService;
    private final LoginUserService loginUserService;
    private final AuthWebMapper authWebMapper;

    public AuthController(
            final RegisterUserService registerUserService,
            final LoginUserService loginUserService,
            final AuthWebMapper authWebMapper) {
        this.registerUserService = registerUserService;
        this.loginUserService = loginUserService;
        this.authWebMapper = authWebMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody final RegisterRequest request) {
        final RegisterUserCommand command = authWebMapper.toRegisterUserCommand(request);
        final RegisterUserResult result = registerUserService.register(command);
        final RegisterResponse response = authWebMapper.toRegisterResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
        final LoginUserCommand command = authWebMapper.toLoginUserCommand(request);
        final LoginUserResult result = loginUserService.login(command);
        final LoginResponse response = authWebMapper.toLoginResponse(result);

        return ResponseEntity.ok(response);
    }
}
