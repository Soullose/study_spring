package com.wsf.infrastructure.security.service;

import com.wsf.infrastructure.security.domain.*;
import com.wsf.domain.entity.Token;
import com.wsf.domain.entity.UserAccount;
import com.wsf.infrastructure.security.enums.TokenType;
import com.wsf.infrastructure.security.repository.TokenRepository;
import com.wsf.infrastructure.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;

    private final TokenRepository tokenRepository;

    private final UserAccountRepository userAccountRepository;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final UserAccountDetailService userAccountDetailService;

    public RegisterResponse register(RegisterRequest request) {
        UserAccount userAccount = UserAccount.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        UserAccount account = userAccountRepository.save(userAccount);

        // 简化注册逻辑，暂时不创建User实体
//		String jwtToken = jwtService.generateToken(new UserAccountDetail(account));
//		saveUserToken(account, jwtToken);
        return RegisterResponse.builder().token(request.getFirstname() + request.getLastname()).build();
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        UserAccountDetail userDetails = userAccountDetailService.loadUserDetailByUsername(request.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);
        revokeAllUserAccountTokens(userDetails.getUserAccount());
        saveUserToken(userDetails.getUserAccount(), jwtToken);
        return AuthenticateResponse.builder().token(jwtToken).build();
    }

    /// 撤销所有token
    private void revokeAllUserAccountTokens(UserAccount account) {
        Set<Token> tokens = tokenRepository.findByUserAccount(account)
                .orElseThrow(NullPointerException::new);
        if (tokens.isEmpty())
            return;
        tokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(tokens);
    }

    /// 保存token
    private void saveUserToken(UserAccount account, String jwtToken) {
//		log.info("{}",LocalDateTime.now(Clock.system(ZoneId.of("CTT",ZoneId.SHORT_IDS))));
        Token token = Token.builder()
                .userAccount(account)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .createDateTime(LocalDateTime.now())
                .build();
        tokenRepository.save(token);
    }
}
