package com.wsf.domain.model.user.aggregate;

import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.IdCardNumber;
import com.wsf.domain.model.user.valueobject.PhoneNumber;
import com.wsf.domain.model.user.valueobject.UserName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 聚合根测试")
class UserTest {

    @Test
    @DisplayName("create()应创建用户并返回全名")
    void should_createUser() {
        UserName name = new UserName("张", "三");
        Email email = new Email("zhangsan@example.com");
        PhoneNumber phone = new PhoneNumber("13800138000");
        IdCardNumber idCard = new IdCardNumber("110101199003071234");

        User user = User.create("U001", name, email, phone, idCard);

        assertThat(user.getId()).isEqualTo("U001");
        assertThat(user.getName().getFullName()).isEqualTo("张三");
        assertThat(user.getEmail().value()).isEqualTo("zhangsan@example.com");
        assertThat(user.getPhoneNumber().value()).isEqualTo("13800138000");
        assertThat(user.getRealName()).isEqualTo("张三");
        assertThat(user.getCreateTime()).isNotNull();
        assertThat(user.getUpdateTime()).isNotNull();
    }

    @Test
    @DisplayName("create()应处理null值对象")
    void should_createUser_withNulls() {
        User user = User.create("U002", new UserName("李", ""), null, null, null);
        assertThat(user.getId()).isEqualTo("U002");
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPhoneNumber()).isNull();
        assertThat(user.getIdCardNumber()).isNull();
    }

    @Test
    @DisplayName("updateProfile()应更新用户资料")
    void should_updateProfile() {
        User user = User.create("U003", new UserName("old", "name"), null, null, null);
        LocalDateTime beforeUpdate = user.getUpdateTime();

        user.updateProfile(new UserName("new", "name"), new Email("new@example.com"), new PhoneNumber("13900000000"));

        assertThat(user.getName().getFullName()).isEqualTo("newname");
        assertThat(user.getRealName()).isEqualTo("newname");
        assertThat(user.getEmail().value()).isEqualTo("new@example.com");
        assertThat(user.getPhoneNumber().value()).isEqualTo("13900000000");
        assertThat(user.getUpdateTime()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("updateProfile()应保留null参数的现有值")
    void should_updateProfile_keepNulls() {
        UserName origName = new UserName("张", "三");
        User user = User.create("U004", origName, null, null, null);
        user.updateProfile(null, null, null);
        assertThat(user.getName()).isEqualTo(origName);
    }

    @Test
    @DisplayName("updateIdCardNumber()应更新身份证号")
    void should_updateIdCardNumber() {
        User user = User.create("U005", new UserName("test", ""), null, null, null);
        IdCardNumber newCard = new IdCardNumber("110101199003071234");
        user.updateIdCardNumber(newCard);
        assertThat(user.getIdCardNumber().value()).isEqualTo("110101199003071234");
    }

    @Test
    @DisplayName("rebuild()应恢复持久化的用户")
    void should_rebuildUser() {
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 6, 1, 10, 0);

        User user = User.rebuild("U006", new UserName("重", "建"),
                null, null, null, "重建", created, updated);

        assertThat(user.getId()).isEqualTo("U006");
        assertThat(user.getFullName()).isEqualTo("重建");
    }
}
