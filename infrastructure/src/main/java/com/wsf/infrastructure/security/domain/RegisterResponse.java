package com.wsf.infrastructure.security.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public record RegisterResponse(String accessToken,String refreshToken) {
}
