package com.wsf.infrastructure.security.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.infrastructure.security.repository.RoleRepository;
import com.wsf.infrastructure.security.repository.UserAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountDetailService {

	private final UserAccountRepository userAccountRepository;
	private final RoleRepository roleRepository;
	private final LoginAttemptService loginAttemptService;
	/**
	 * 查询用户明细数据包括角色等等
	 *
	 * @param username 用户名
	 * @return	{@link UserAccountDetail}
	 */
	public UserAccountDetail loadUserDetailByUsername(String username) {
		loginAttemptService.hasAttemptsLocked(username);
//		if (username != null) {
//			throw new LockedException("用户被锁定");
//		}
		var userAccount = userAccountRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

		// 优雅处理角色查询，即使没有角色也不抛出异常
		var roles = roleRepository.findByUserAccounts(userAccount)
				.orElse(Collections.emptySet());

		log.debug("roles:{}", roles.size());

		userAccount.setRoles(roles);

		log.debug("userAccount-roles:{}", userAccount.getRoles().size());
		return new UserAccountDetail(userAccount);
		//        return loadUserDetails(username);
	}

}
