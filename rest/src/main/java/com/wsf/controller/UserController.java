package com.wsf.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    /**
     * 创建用户
     * POST /api/v1/users
     *
     * @param request 创建用户请求体
     * @return 创建成功的用户信息
     */
    @PostMapping()
    private void createUser() {
    }

    /**
     * 查询用户列表
     * GET /api/v1/users
     *
     * @return 用户列表
     */
    @GetMapping()
    private void getUsers() {
    }

    /**
     * 查询用户详情
     * GET /api/v1/users/{userId}
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    private void getUserById(@PathVariable @NotBlank(message = "用户ID不能为空") String userId) {
    }

    /**
     * 更新用户
     * PUT /api/v1/users/{userId}
     *
     * @param userId  用户ID
     * @param request 更新用户请求体
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}")
    private void updateUser() {
    }

    /**
     * 删除用户
     * DELETE /api/v1/users/{userId}
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    private void deleteUser(@PathVariable @NotBlank(message = "用户ID不能为空") String userId) {
    }

    /**
     * 为用户创建账户
     * POST /api/v1/users/{userId}/account
     *
     * @param userId    用户ID
     * @param accountId 账户ID（请求体）
     * @return 操作结果
     */
    @PostMapping("/{userId}/account")
    private void createAccountForUser() {
    }

    /**
     * 解除用户账户关联
     * DELETE /api/v1/users/{userId}/account
     *
     * @param userId    用户ID
     * @param accountId 账户ID（请求体）
     * @return 操作结果
     */
    @DeleteMapping("/{userId}/account")
    private void removeAccountFromUser() {
    }
}
