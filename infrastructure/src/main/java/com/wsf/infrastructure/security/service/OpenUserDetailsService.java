package com.wsf.infrastructure.security.service;

import com.wsf.entity.UserAccount;
import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenUserDetailsService implements UserDetailsService {

	private final UserAccountRepository repository;

	@Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserAccount userAccount = repository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
		return new UserAccountDetail(userAccount);
	}
}