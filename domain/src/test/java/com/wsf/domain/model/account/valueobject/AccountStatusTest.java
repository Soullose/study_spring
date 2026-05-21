package com.wsf.domain.model.account.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AccountStatus 值对象测试")
class AccountStatusTest {

    @Test
    @DisplayName("normal()应创建全部正常的状态")
    void should_createNormalStatus() {
        AccountStatus status = AccountStatus.normal();
        assertThat(status.enabled()).isTrue();
        assertThat(status.accountNonExpired()).isTrue();
        assertThat(status.accountNonLocked()).isTrue();
        assertThat(status.credentialsNonExpired()).isTrue();
        assertThat(status.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("disabled()应创建禁用状态")
    void should_createDisabledStatus() {
        AccountStatus status = AccountStatus.disabled();
        assertThat(status.enabled()).isFalse();
        assertThat(status.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("locked()应创建锁定状态")
    void should_createLockedStatus() {
        AccountStatus status = AccountStatus.locked();
        assertThat(status.accountNonLocked()).isFalse();
        assertThat(status.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("expired()应创建过期状态")
    void should_createExpiredStatus() {
        AccountStatus status = AccountStatus.expired();
        assertThat(status.accountNonExpired()).isFalse();
        assertThat(status.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("credentialsExpired()应创建凭证过期状态")
    void should_createCredentialsExpiredStatus() {
        AccountStatus status = AccountStatus.credentialsExpired();
        assertThat(status.credentialsNonExpired()).isFalse();
        assertThat(status.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("enable()应启用账户")
    void should_enableAccount() {
        AccountStatus disabled = AccountStatus.disabled();
        AccountStatus enabled = disabled.enable();
        assertThat(enabled.enabled()).isTrue();
    }

    @Test
    @DisplayName("disable()应禁用账户")
    void should_disableAccount() {
        AccountStatus normal = AccountStatus.normal();
        AccountStatus disabled = normal.disable();
        assertThat(disabled.enabled()).isFalse();
    }

    @Test
    @DisplayName("lock()应锁定账户")
    void should_lockAccount() {
        AccountStatus normal = AccountStatus.normal();
        AccountStatus locked = normal.lock();
        assertThat(locked.accountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("unlock()应解锁账户")
    void should_unlockAccount() {
        AccountStatus locked = AccountStatus.locked();
        AccountStatus unlocked = locked.unlock();
        assertThat(unlocked.accountNonLocked()).isTrue();
    }
}
