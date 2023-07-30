package com.wsf.infrastructure.security.service;

import com.wsf.entity.Token;
import com.wsf.entity.User;
import com.wsf.entity.UserAccount;
import com.wsf.enums.TokenType;
import com.wsf.infrastructure.security.domain.*;
import com.wsf.repository.TokenRepository;
import com.wsf.repository.UserAccountRepository;
import com.wsf.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final PasswordEncoder passwordEncoder;

	private final UserRepository userRepository;

	//	private final RoleRepository roleRepository;

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

		User user = User.builder()
				.firstname(request.getFirstname())
				.lastname(request.getLastname())
				.email(request.getEmail())
				.userAccount(account)
				.build();
		userRepository.save(user);
		String jwtToken = jwtService.generateToken(new UserAccountDetail(account));
		saveUserToken(account, jwtToken);
		return RegisterResponse.builder().token(jwtToken).build();
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

	///撤销所有token
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

	///保存token
	private void saveUserToken(UserAccount account, String jwtToken) {
		Token token = Token.builder()
				.userAccount(account)
				.token(jwtToken)
				.tokenType(TokenType.BEARER)
				.expired(false)
				.revoked(false)
				.build();
		tokenRepository.save(token);
	}
}
