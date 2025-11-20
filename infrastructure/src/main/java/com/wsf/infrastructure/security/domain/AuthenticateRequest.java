package com.wsf.infrastructure.security.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticateRequest {
	private String username;
	private String password;
	@Builder.Default
	private boolean rememberMe = false;
}
