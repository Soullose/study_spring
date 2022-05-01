package com.wsf.repository;

/**
 * open
 * SoulLose
 * 2022-04-28 09:45
 */
import com.wsf.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,String> {
    
    User getUserByUserName(String userName);
    
}
