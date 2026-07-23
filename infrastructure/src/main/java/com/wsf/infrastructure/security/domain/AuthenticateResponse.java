package com.wsf.infrastructure.security.domain;

import lombok.Builder;

@Builder
public record AuthenticateResponse(String accessToken, String refreshToken) {
}
