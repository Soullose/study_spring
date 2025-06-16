package com.wsf.infrastructure.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 验证码异常
 */
public class VerificationCodeException extends AuthenticationException {
	public VerificationCodeException(String msg) {
		super(msg);
	}
}
