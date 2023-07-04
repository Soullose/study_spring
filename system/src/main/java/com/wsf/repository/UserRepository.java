package com.wsf.repository;

/**
 * open
 * SoulLose
 * 2022-04-28 09:45
 */

import com.wsf.entity.User;
import com.wsf.jpa.OpenRepository;

public interface UserRepository extends OpenRepository<User> {

    User findUserByUserName(String userName);

}
