package com.wsf.infrastructure.security.service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
  @Value("${security.jwt.secret}")
  private String secret;
  @Value("${security.jwt.access-token-ttl}")
  private Duration accessTtl;
  @Value("${security.jwt.refresh-token-ttl}")
  private Duration refreshTtl;

  public String generateAccessToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  /// 生成
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts.builder().header().type("JWT").and()
        .issuer("w2")
        .subject(userDetails.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessTtl.toMillis()))
        .audience().add("w2-server").and()
        .id(UUID.randomUUID().toString())
        .signWith(SignInKey(), Jwts.SIG.HS256)
        .compact();
  }

  public String generateRefreshToken(UserDetails ud) {
    return Jwts.builder().subject(ud.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + refreshTtl.toMillis()))
        .id(UUID.randomUUID().toString()) // jti，Redis 比对的凭据
        .signWith(SignInKey(), Jwts.SIG.HS256).compact();
  }

  public String extractJti(String token) {
    return extractClaim(token, Claims::getId);
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /// 解密
  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(SignInKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey SignInKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));

  }
}
