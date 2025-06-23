package com.wsf.infrastructure.security.service;

import java.util.Set;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.wsf.entity.Role;
import com.wsf.entity.UserAccount;
import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.repository.RoleRepository;
import com.wsf.repository.UserAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountDetailService {

	private final UserAccountRepository userAccountRepository;
	private final RoleRepository roleRepository;

	/**
	 * 查询用户明细数据包括角色等等
	 *
	 * @param username 用户名
	 * @return	{@link UserAccountDetail}
	 */
	public UserAccountDetail loadUserDetailByUsername(String username) {
//		if (username != null) {
//			throw new LockedException("用户被锁定");
//		}
		UserAccount userAccount = userAccountRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

		Set<Role> roles = roleRepository.findByUserAccounts(userAccount)
				.orElseThrow(NullPointerException::new);

		log.debug("roles:{}", roles.size());

		userAccount.setRoles(roles);

		log.debug("userAccount-roles:{}", userAccount.getRoles().size());
		return new UserAccountDetail(userAccount);
		//        return loadUserDetails(username);
	}

}
