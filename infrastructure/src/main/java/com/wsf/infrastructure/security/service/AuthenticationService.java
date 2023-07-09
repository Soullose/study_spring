package com.wsf.infrastructure.security.service;

import com.wsf.entity.User;
import com.wsf.entity.UserAccount;
import com.wsf.infrastructure.security.domain.*;
import com.wsf.repository.UserAccountRepository;
import com.wsf.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final PasswordEncoder passwordEncoder;

	private final UserRepository userRepository;

	private final UserAccountRepository userAccountRepository;

	private final JwtService jwtService;

	private final AuthenticationManager authenticationManager;

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
		String token = jwtService.generateToken(new UserAccountDetail(account));
		return RegisterResponse.builder().token(token).build();
	}

	public AuthenticateResponse authenticate(AuthenticateRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getUsername(),
						request.getPassword()
				)
		);

		UserAccount userAccount = userAccountRepository.findByUsername(request.getUsername())
				.orElseThrow(null);
		String token = jwtService.generateToken(new UserAccountDetail(userAccount));
		return AuthenticateResponse.builder().token(token).build();
	}
}
