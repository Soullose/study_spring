package com.wsf.infrastructure.security.auth;

import com.wsf.infrastructure.common.result.Result;
import com.wsf.infrastructure.security.domain.AuthenticateRequest;
import com.wsf.infrastructure.security.domain.AuthenticateResponse;
import com.wsf.infrastructure.security.domain.RegisterRequest;
import com.wsf.infrastructure.security.domain.RegisterResponse;
import com.wsf.infrastructure.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    /// 注册
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    /// 认证
    @Deprecated
    @PostMapping("/authenticate")
    public Result<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request) {
        return Result.success(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken() {
        return ResponseEntity.ok("");
    }

//	@PostMapping("/refresh-token")
//	public Result<TokenPair> refreshToken(@RequestBody @Validated RefreshRequest req) {
//		return Result.success(service.refresh(req.getRefreshToken()));
//	}
}
