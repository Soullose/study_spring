package com.wsf.domain.model.account.aggregate;

import com.wsf.domain.model.account.valueobject.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserAccount 聚合根测试")
class UserAccountTest {

    @Test
    @DisplayName("create()应创建账户")
    void should_createAccount() {
        Password pwd = new Password("password123", false);
        UserAccount account = UserAccount.create("A001", "admin", pwd, "U001");

        assertThat(account.getId()).isEqualTo("A001");
        assertThat(account.getUsername()).isEqualTo("admin");
        assertThat(account.getUserId()).isEqualTo("U001");
        assertThat(account.isAvailable()).isTrue();
        assertThat(account.getCreateTime()).isNotNull();
    }

    @Test
    @DisplayName("create()应抛出异常 when 用户名为空")
    void should_throwException_when_usernameEmpty() {
        Password pwd = new Password("password123", false);
        assertThatThrownBy(() -> UserAccount.create("A001", null, pwd, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be empty");
    }

    @Test
    @DisplayName("updatePassword()应更新密码")
    void should_updatePassword() {
        Password pwd = new Password("oldpass12", false);
        UserAccount account = UserAccount.create("A002", "user", pwd, null);

        Password newPwd = Password.ofEncoded("$argon2id$newhash");
        account.updatePassword(newPwd);

        assertThat(account.getPassword()).isEqualTo(newPwd);
    }

    @Test
    @DisplayName("linkUser()应关联用户")
    void should_linkUser() {
        UserAccount account = UserAccount.create("A003", "user", new Password("pass12345", false), null);
        account.linkUser("U005");
        assertThat(account.getUserId()).isEqualTo("U005");
    }

    @Test
    @DisplayName("unlinkUser()应解除用户关联")
    void should_unlinkUser() {
        UserAccount account = UserAccount.create("A004", "user", new Password("pass12345", false), "U001");
        account.unlinkUser();
        assertThat(account.getUserId()).isNull();
    }

    @Test
    @DisplayName("状态管理：启用/禁用/锁定/解锁")
    void should_manageStatus() {
        UserAccount account = UserAccount.create("A005", "user", new Password("pass12345", false), null);

        account.disable();
        assertThat(account.isAvailable()).isFalse();

        account.enable();
        assertThat(account.isAvailable()).isTrue();

        account.lock();
        assertThat(account.isAvailable()).isFalse();

        account.unlock();
        assertThat(account.isAvailable()).isTrue();
    }
}
