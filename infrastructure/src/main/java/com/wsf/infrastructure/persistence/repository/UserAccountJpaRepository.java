package com.wsf.infrastructure.persistence.repository;
import com.wsf.infrastructure.persistence.entity.user.UserAccountPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户账户JPA仓储
 */
@Repository
public interface UserAccountJpaRepository 
        extends JpaRepository<UserAccountPO, String>,
                JpaSpecificationExecutor<UserAccountPO>,
                QuerydslPredicateExecutor<UserAccountPO> {

    Optional<UserAccountPO> findByUsername(String userName);
    Optional<UserAccountPO> findByUserId(String userId);

    /**
     * 根据角色ID查找账户列表
     */
    @Query("SELECT ua FROM UserAccountPO ua JOIN ua.roles r WHERE r.id = :roleId")
    List<UserAccountPO> findByRoleId(String roleId);
}
