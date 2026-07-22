package com.wsf.infrastructure.security.domain;

public record TokenPair(String accessToken, String refreshToken) {
}
