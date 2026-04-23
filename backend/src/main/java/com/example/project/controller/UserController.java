package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.dto.UpdateAvatarRequestDTO;
import com.example.project.dto.UpdatePasswordRequestDTO;
import com.example.project.entity.User;
import com.example.project.service.UserService;
import com.example.project.shiro.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * <p>
 * 提供用户信息管理的API接口，包括获取用户资料、更新个人资料、修改头像和密码等功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户的个人资料
     *
     * @return 当前用户信息
     */
    @GetMapping("/profile")
    public Result<User> getProfile() {
        Long userId = UserContext.get().getId();
        return userService.getUserById(userId);
    }

    /**
     * 获取指定用户的公开资料
     *
     * @param id 用户ID
     * @return 用户公开资料信息
     */
    @GetMapping("/{id}")
    public Result<User> getPublicProfile(@PathVariable Long id) {
        Long viewerId = null;
        User currentUser = UserContext.get();
        if (currentUser != null) {
            viewerId = currentUser.getId();
        }
        return userService.getPublicProfile(id, viewerId);
    }

    /**
     * 更新当前用户的个人资料
     *
     * @param user 包含更新信息的用户对象
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user) {
        Long userId = UserContext.get().getId();
        return userService.updateProfile(userId, user);
    }

    /**
     * 更新当前用户的头像
     *
     * @param request 包含avatar字段的请求体，avatar为新头像的URL
     * @return 更新后的用户信息
     */
    @PutMapping("/avatar")
    public Result<User> updateAvatar(@RequestBody UpdateAvatarRequestDTO request) {
        Long userId = UserContext.get().getId();
        return userService.updateAvatar(userId, request.getAvatar());
    }

    /**
     * 修改当前用户的密码
     *
     * @param request 包含oldPassword和newPassword字段的请求体
     * @return 操作结果
     */
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody UpdatePasswordRequestDTO request) {
        Long userId = UserContext.get().getId();
        return userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
    }
}
