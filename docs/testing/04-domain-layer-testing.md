# 04 — 领域层测试实战

> 预计阅读：30 分钟 | 难度：★★☆☆☆ | 对应模块：`domain/`

---

## 1. 领域层测试的特点

领域层（Domain Layer）包含：
- **值对象（Value Object）**：Email、Password、PhoneNumber 等
- **聚合根（Aggregate Root）**：User、Role、Menu、UserAccount
- **实体（Entity）**：Permission、DataPermission
- **领域事件（Domain Event）**：UserCreatedEvent、RoleAssignedEvent 等

**测试特点**：
- ✅ **不需要 Spring 容器** — 纯 Java 对象，`new` 出来就能测
- ✅ **不需要 Mock** — 值对象没有外部依赖
- ✅ **运行极快** — 几十毫秒就能跑完
- ⚠️ 聚合根如果依赖 `IdGenerator` 等接口，才需要 Mock

---

## 2. 值对象测试 — 以 `EmailTest` 为例

值对象是最简单的测试目标。它们在构造函数中做校验，没有任何依赖。

### 完整范例：`domain/src/test/java/.../EmailTest.java`

```java
package com.wsf.domain.model.user.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email 值对象测试")
class EmailTest {

    // ==================== 正常用例 ====================

    @Test
    @DisplayName("应创建Email对象 when 邮箱格式有效")
    void should_createEmail_when_emailFormatValid() {
        Email email = new Email("test@example.com");

        assertThat(email.value()).isEqualTo("test@example.com");
        assertThat(email.isEmpty()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "user@domain.com",
        "a@b.co",
        "test.user@company.org",
        "user+tag@example.com",
        "user123@test.co.uk"
    })
    @DisplayName("应接受多种有效邮箱格式")
    void should_accept_validEmailFormats(String validEmail) {
        assertThatCode(() -> new Email(validEmail))
            .doesNotThrowAnyException();
    }

    // ==================== 异常用例 ====================

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "@no-local",
        "no-at-sign",
        "no@tld.",
        "@.com",
        " spaces@test.com"
    })
    @DisplayName("应抛出异常 when 邮箱格式无效")
    void should_throwException_when_emailFormatInvalid(String invalidEmail) {
        assertThatThrownBy(() -> new Email(invalidEmail))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email format");
    }

    // ==================== 边界用例 ====================

    @Test
    @DisplayName("应接受 null 值")
    void should_acceptNull() {
        Email email = new Email(null);
        assertThat(email.value()).isNull();
        assertThat(email.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("应接受空字符串")
    void should_acceptEmptyString() {
        Email email = new Email("");
        assertThat(email.isEmpty()).isTrue();
    }

    // ==================== 静态工厂方法 ====================

    @Test
    @DisplayName("of静态方法应创建Email")
    void should_createViaOf() {
        Email email = Email.of("hello@world.com");
        assertThat(email.value()).isEqualTo("hello@world.com");
    }
}
```

### 值对象测试模板

```java
@DisplayName("XXX 值对象测试")
class XxxTest {

    // 1. 正常情况：传入合法值，创建成功
    @Test
    void should_create_when_inputValid() {
        Xxx obj = new Xxx(validInput);
        assertThat(obj.value()).isEqualTo(validInput);
    }

    // 2. 异常情况：传入非法值，抛异常
    @Test
    void should_throwException_when_inputInvalid() {
        assertThatThrownBy(() -> new Xxx(invalidInput))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // 3. 边界情况：null、空字符串、超长值
    @Test
    void should_handleNull() { ... }
    @Test
    void should_handleEmpty() { ... }

    // 4. 相等性：相同值应相等
    @Test
    void should_beEqual_when_sameValue() {
        assertThat(new Xxx("a")).isEqualTo(new Xxx("a"));
    }
}
```

---

## 3. 聚合根测试 — 以 `RoleTest` 为例

聚合根测试的重点是**业务行为**：创建、更新、分配关联、状态切换。

### 完整范例：`domain/src/test/java/.../RoleTest.java`

```java
package com.wsf.domain.model.role.aggregate;

import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.role.valueobject.RoleCode;
import com.wsf.domain.model.role.valueobject.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Role 聚合根测试")
class RoleTest {

    // ==================== 创建 ====================

    @Test
    @DisplayName("create()应创建启用的角色")
    void should_createRole() {
        Role role = Role.create("R001", new RoleCode("ADMIN"),
            new RoleName("管理员"), "系统管理员");

        assertThat(role.getId()).isEqualTo("R001");
        assertThat(role.getCode().value()).isEqualTo("ADMIN");
        assertThat(role.getName().value()).isEqualTo("管理员");
        assertThat(role.isEnabled()).isTrue();         // 默认启用
        assertThat(role.getMenus()).isEmpty();          // 初始无菜单
        assertThat(role.getDataPermissions()).isEmpty(); // 初始无权限
    }

    // ==================== 更新 ====================

    @Test
    @DisplayName("update()应更新名称和描述")
    void should_updateRole() {
        Role role = Role.create("R002", new RoleCode("USER"),
            new RoleName("用户"), "普通用户");

        role.update(new RoleName("普通用户"), "更新后的描述");

        assertThat(role.getName().value()).isEqualTo("普通用户");
        assertThat(role.getDescription()).isEqualTo("更新后的描述");
    }

    // ==================== 状态切换 ====================

    @Test
    @DisplayName("enable()/disable()应切换启用状态")
    void should_toggleEnabled() {
        Role role = Role.create("R003", new RoleCode("GUEST"),
            new RoleName("访客"), "");

        role.disable();
        assertThat(role.isEnabled()).isFalse();

        role.enable();
        assertThat(role.isEnabled()).isTrue();
    }

    // ==================== 分配菜单 ====================

    @Test
    @DisplayName("assignMenu()应分配菜单")
    void should_assignMenu() {
        Role role = Role.create("R004", new RoleCode("MANAGER"),
            new RoleName("经理"), "");
        Menu menu = Menu.createMenu("M001", "首页", null,
            "/home", "Home", "sys:home:view", "home", 1);

        role.assignMenu(menu);

        assertThat(role.getMenus()).hasSize(1);
        assertThat(role.getMenuIds()).contains("M001");
    }

    @Test
    @DisplayName("removeMenu()应移除菜单")
    void should_removeMenu() {
        Role role = Role.create("R005", new RoleCode("OP"),
            new RoleName("操作员"), "");
        Menu menu = Menu.createMenu("M002", "用户管理", null,
            "/users", "Users", "sys:user:list", "user", 1);
        role.assignMenu(menu);

        role.removeMenu("M002");

        assertThat(role.getMenus()).isEmpty();
    }

    @Test
    @DisplayName("assignMenus()应批量分配菜单")
    void should_assignMenus() {
        Role role = Role.create("R006", new RoleCode("SUPER"),
            new RoleName("超级管理员"), "");
        Menu m1 = Menu.createButton("M003", "新增", "M002",
            "sys:user:create", 1);
        Menu m2 = Menu.createButton("M004", "删除", "M002",
            "sys:user:delete", 2);

        role.assignMenus(Set.of(m1, m2));

        assertThat(role.getMenus()).hasSize(2);
    }

    // ==================== 从持久化重建 ====================

    @Test
    @DisplayName("rebuild()应恢复持久化的角色")
    void should_rebuildRole() {
        Role role = Role.rebuild("R009", new RoleCode("OLD"),
            new RoleName("旧角色"), "desc", true,
            java.time.LocalDateTime.now(), java.time.LocalDateTime.now());

        assertThat(role.getId()).isEqualTo("R009");
        assertThat(role.isEnabled()).isTrue();
    }
}
```

### 聚合根测试模板

```java
@DisplayName("XXX 聚合根测试")
class XxxTest {

    // 1. 创建
    @Test void should_create() { ... }

    // 2. 每个业务方法一个测试
    @Test void should_update() { ... }
    @Test void should_enable() { ... }
    @Test void should_disable() { ... }

    // 3. 关联操作
    @Test void should_assignRelatedObjects() { ... }
    @Test void should_removeRelatedObjects() { ... }

    // 4. 从持久化重建（rebuild 模式）
    @Test void should_rebuildFromPersistence() { ... }

    // 5. 不变量/业务规则校验
    @Test void should_throwException_when_invariantViolated() { ... }
}
```

---

## 4. 领域事件测试 — 以 `BaseDomainEventTest` 为例

```java
@DisplayName("BaseDomainEvent 测试")
class BaseDomainEventTest {

    @Test
    @DisplayName("应记录事件发生时间")
    void should_recordOccurredTime() {
        TestEvent event = new TestEvent("source");
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    @DisplayName("应记录事件源")
    void should_recordSource() {
        TestEvent event = new TestEvent("source-id");
        assertThat(event.getSource()).isEqualTo("source-id");
    }

    // 测试用的具体事件类
    static class TestEvent extends BaseDomainEvent {
        public TestEvent(Object source) {
            super(source);
        }
    }
}
```

---

## 5. 编写领域层测试的步骤（手把手）

假设你要给一个新的值对象 `PhoneNumber` 写测试。以下是完整步骤：

### Step 1：创建测试文件

```
src/main/java/com/wsf/domain/model/user/valueobject/PhoneNumber.java   ← 被测类
src/test/java/com/wsf/domain/model/user/valueobject/PhoneNumberTest.java  ← 测试类
```

> 注意：`src/test` 和 `src/main` 的**包路径完全一致**。

### Step 2：搭框架

```java
package com.wsf.domain.model.user.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PhoneNumber 值对象测试")
class PhoneNumberTest {
    // 测试方法写在这里
}
```

### Step 3：先看被测类的构造函数和行为

```java
// PhoneNumber.java
public class PhoneNumber {
    private final String value;

    public PhoneNumber(String value) {
        if (value != null && !isValid(value)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.value = value;
    }

    public String value() { return value; }
    public boolean isEmpty() { return value == null || value.isEmpty(); }
}
```

### Step 4：写测试方法

```java
class PhoneNumberTest {

    @Test
    @DisplayName("应创建手机号 when 格式有效")
    void should_create_when_formatValid() {
        PhoneNumber phone = new PhoneNumber("13800138000");
        assertThat(phone.value()).isEqualTo("13800138000");
    }

    @ParameterizedTest
    @ValueSource(strings = {"13800138000", "19912345678", "18600001111"})
    @DisplayName("应接受多种有效手机号")
    void should_accept_validNumbers(String num) {
        assertThatCode(() -> new PhoneNumber(num)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "abc", "1380013800a", "123456789012"})
    @DisplayName("应抛出异常 when 格式无效")
    void should_throwException_when_invalid(String num) {
        assertThatThrownBy(() -> new PhoneNumber(num))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("应接受 null")
    void should_acceptNull() {
        PhoneNumber phone = new PhoneNumber(null);
        assertThat(phone.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("应接受空字符串")
    void should_acceptEmpty() {
        PhoneNumber phone = new PhoneNumber("");
        assertThat(phone.isEmpty()).isTrue();
    }
}
```

### Step 5：运行验证

```bash
mvn test -pl domain -Dtest=PhoneNumberTest
```

---

## 6. 领域层测试的 `pom.xml` 依赖

领域层测试只需要两个依赖（本项目已配置好）：

```xml
<!-- domain/pom.xml -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

**显式引入而非依赖 starter** 是为了保持领域层纯粹（理论上领域层不应该依赖 Spring 框架）。

---

## 7. 本模块已有测试清单

| 测试类 | 测试对象 | 行数 |
|--------|----------|:---:|
| `EmailTest` | Email 值对象 | ~73 |
| `PhoneNumberTest` | PhoneNumber 值对象 | 相似 |
| `UserNameTest` | UserName 值对象 | 相似 |
| `IdCardNumberTest` | IdCardNumber 值对象 | 相似 |
| `PasswordTest` | Password 值对象 | 相似 |
| `AccountStatusTest` | AccountStatus 值对象 | 相似 |
| `RoleCodeTest` | RoleCode 值对象 | 相似 |
| `RoleNameTest` | RoleName 值对象 | 相似 |
| `MenuTypeTest` | MenuType 值对象 | 相似 |
| `MenuStatusTest` | MenuStatus 值对象 | 相似 |
| `DataScopeTest` | DataScope 值对象 | 相似 |
| `ResourceTypeTest` | ResourceType 值对象 | 相似 |
| `UserTest` | User 聚合根 | — |
| `UserAccountTest` | UserAccount 聚合根 | — |
| `RoleTest` | Role 聚合根 | ~119 |
| `MenuTest` | Menu 聚合根 | — |
| `PermissionTest` | Permission 实体 | — |
| `DataPermissionTest` | DataPermission 实体 | — |
| `BaseDomainEventTest` | BaseDomainEvent | — |

> 📊 领域层测试覆盖率很高，是项目的标杆模块。

---

## 8. 小结

| 要点 | 说明 |
|------|------|
| 不需要 Spring | `new` 出来就能测 |
| 测试覆盖三要素 | 正常 → 边界 → 异常 |
| 参数化测试 | 多组数据用 `@ParameterizedTest` |
| 值对象模板 | 创建成功 / 格式无效抛异常 / null 和 空处理 |
| 聚合根模板 | 创建 / 每个业务方法 / 关联操作 / rebuild |

---

## 下一步

领域层是最容易写测试的。下一章看有外部依赖的 **[05 - 应用层测试实战](05-app-layer-testing.md)**，开始用到 Mockito！
