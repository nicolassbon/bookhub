package com.bookhub.identity.web.auth;

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

    public AuthController(final RegisterUserService registerUserService) {
        this.registerUserService = registerUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody final RegisterRequest request) {
        final RegisterUserResult result = registerUserService.register(RegisterUserCommand.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .displayName(request.displayName())
                .build());

        final RegisterResponse response = RegisterResponse.builder()
                .userId(result.userId())
                .username(result.username())
                .email(result.email())
                .displayName(result.displayName())
                .role(result.role())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
