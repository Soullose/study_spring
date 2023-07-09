package com.wsf.infrastructure.security.auth;

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

	///注册
	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(service.register(request));
	}

	///认证
	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request) {
		return ResponseEntity.ok(service.authenticate(request));
	}
}
