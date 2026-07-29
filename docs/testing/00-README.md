# Spring Boot 测试用例编写手册

> 面向**零基础**开发者，手把手教你为 Spring Boot 多模块项目编写测试。
> 每章均以本项目 `study_spring` 中的**真实代码**作为范例。

---

## 阅读路线

本手册按 **先打基础 → 再实战** 的顺序编排，建议按编号依次阅读：

### 🔰 基础篇（必读）

| 章节 | 内容 | 预计时间 |
|:----:|------|:--------:|
| [01 - JUnit 5 零基础](01-junit5-basics.md) | `@Test`、`@BeforeEach`、`@DisplayName`、参数化测试 | 30 分钟 |
| [02 - AssertJ 断言指南](02-assertj-guide.md) | `assertThat()` 链式断言、字符串/集合/异常断言 | 20 分钟 |
| [03 - Mockito 模拟指南](03-mockito-guide.md) | `@Mock`、`@InjectMocks`、`when/verify`、参数匹配器 | 25 分钟 |

### 🏗️ 实战篇（按项目分层）

| 章节 | 内容 | 测试对象 |
|:----:|------|----------|
| [04 - 领域层测试](04-domain-layer-testing.md) | 值对象 + 聚合根 + 领域事件 | `domain/` 模块 |
| [05 - 应用层测试](05-app-layer-testing.md) | Mock 仓储 + 业务流程编排 | `app/` 模块 |
| [06 - 基础设施层测试](06-infrastructure-testing.md) | Converter + Repository + Security | `infrastructure/` 模块 |
| [07 - 控制器层测试](07-controller-testing.md) | `@WebMvcTest` + `MockMvc` | `rest/` 模块 |
| [08 - 集成测试](08-integration-testing.md) | `@SpringBootTest` + 数据库 | 全栈 |

### 🛠️ 工具篇

| 章节 | 内容 |
|:----:|------|
| [09 - 速查表](09-cheatsheet.md) | 常用注解大全、断言方法速查、Mockito 模式速查 |

---

## 前置知识

- 了解 Java 基础语法（类、方法、注解）
- 了解 Maven 基本概念（`pom.xml`、依赖管理）
- 了解本项目的基本结构（参考 [启动说明](../../启动说明.md) 和 [README](../../README.md)）

## 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | **21** | 项目 `pom.xml` 要求 `maven.compiler.source=21` |
| Maven | 3.6+ | 构建和运行测试 |
| IDE | IntelliJ IDEA / VS Code | IDEA 对 JUnit 支持最好 |

### 本机快速配置 JDK 21

```bash
# Windows Git Bash 中临时切换
export JAVA_HOME="E:/dev/sdk/jdk-21.0.11+10"
export PATH="$JAVA_HOME/bin:$PATH"
java -version  # 确认版本
```

---

## 本项目测试依赖一览

测试相关依赖已在各模块 `pom.xml` 中配置，无需额外安装：

```xml
<!-- JUnit 5（Jupiter） -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- AssertJ：链式断言 -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Boot Test（含 Mockito） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

`spring-boot-starter-test` 是一个**聚合依赖**，已包含：
- JUnit 5
- Mockito
- AssertJ
- Hamcrest
- JSONassert
- Spring Test

---

## 快速运行测试

```bash
# 运行全部测试
mvn test

# 只运行某个模块的测试
mvn test -pl domain

# 运行特定测试类
mvn test -pl domain -Dtest=EmailTest

# 运行特定测试方法
mvn test -pl domain -Dtest=EmailTest#should_createEmail_when_emailFormatValid

# 生成覆盖率报告
mvn test jacoco:report
# 报告位置：各模块 target/site/jacoco/index.html
```

---

## 测试文件命名规范

本项目遵循以下约定：

| 被测类 | 测试类 | 位置 |
|--------|--------|------|
| `src/main/java/.../Email.java` | `src/test/java/.../EmailTest.java` | 同包不同源根 |
| `src/main/java/.../UserServiceImpl.java` | `src/test/java/.../UserServiceImplTest.java` | 同包不同源根 |

即：测试类名 = 被测类名 + `Test`，放在 `src/test/java` 下相同包路径。

---

## 下一步

现在打开 [01 - JUnit 5 零基础](01-junit5-basics.md)，开始你的第一个测试！
