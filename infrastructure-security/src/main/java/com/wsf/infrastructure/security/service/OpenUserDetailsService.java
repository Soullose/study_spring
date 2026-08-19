package com.wsf.infrastructure.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenUserDetailsService implements UserDetailsService {

//	private final UserAccountRepository repository;

    private final UserAccountDetailService service;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		UserAccount userAccount = repository.findByUsername(username)
//				.orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
//		return new UserAccountDetail(userAccount);
//
        return service.loadUserDetailByUsername(username);
    }
}