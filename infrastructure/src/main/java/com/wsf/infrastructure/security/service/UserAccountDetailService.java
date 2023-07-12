package com.wsf.infrastructure.security.service;

import com.wsf.entity.Role;
import com.wsf.entity.UserAccount;
import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.repository.RoleRepository;
import com.wsf.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountDetailService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;

    /**
     * 查询用户明细数据包括角色等等
     * @param username      用户名
     * @return
     */
    public UserDetails loadUserDetailByUsername(String username) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NullPointerException("用户不存在"));

        Set<Role> roles = roleRepository.findByUserAccounts(userAccount)
                .orElseThrow(NullPointerException::new);

        log.debug("roles:{}", roles.size());

        userAccount.setRoles(roles);

        log.debug("userAccount-roles:{}", userAccount.getRoles().size());

        return new UserAccountDetail(userAccount);
//        return loadUserDetails(username);
    }

//    private UserAccountDetail loadUserDetails(String username) {
//        UserAccount userAccount = userAccountRepository.findByUsername(username)
//                .orElseThrow(() -> new NullPointerException("用户不存在"));
//
//        Set<Role> roles = roleRepository.findByUserAccounts(userAccount)
//                .orElseThrow(NullPointerException::new);
//
//
//        return new UserAccountDetail(userAccount);
//    }
}
