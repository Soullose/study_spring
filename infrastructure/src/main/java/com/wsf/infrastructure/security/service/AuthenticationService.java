package com.wsf.infrastructure.security.service;

import com.wsf.entity.User;
import com.wsf.entity.UserAccount;
import com.wsf.infrastructure.security.domain.*;
import com.wsf.repository.UserAccountRepository;
import com.wsf.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

//	private final RoleRepository roleRepository;

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

//		UserAccount userAccount = userAccountRepository.findByUsername(request.getUsername())
//				.orElseThrow(null);
//
//		Set<Role> roles = roleRepository.findByUserAccounts(userAccount).orElseThrow(NullPointerException::new);
//
//		log.debug("roles:{}",roles.size());
//
//		userAccount.setRoles(roles);
//
//		log.debug("userAccount-roles:{}",userAccount.getRoles().size());
//
//		userAccount.setRoles(roles);
        UserDetails userDetails = userAccountDetailService.loadUserDetailByUsername(request.getUsername());
//		log.debug("AuthenticationService-userDetails:{}",userDetails);
        String token = jwtService.generateToken(userDetails);
//		String token = jwtService.generateToken(new UserAccountDetail(userAccount));
        return AuthenticateResponse.builder().token(token).build();
    }
}
