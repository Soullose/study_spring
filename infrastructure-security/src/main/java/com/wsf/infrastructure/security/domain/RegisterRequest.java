package com.wsf.infrastructure.security.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
	@NotBlank
	private String username;
	@NotBlank
	private String password;
	@NotBlank
	private String firstname;
	@NotBlank
	private String lastname;
	private String email;
}
