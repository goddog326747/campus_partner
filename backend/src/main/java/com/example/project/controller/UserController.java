package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.entity.User;
import com.example.project.service.UserService;
import com.example.project.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public Result<User> getProfile() {
        Long userId = UserContext.get().getId();
        return userService.getUserById(userId);
    }

    @GetMapping("/{id}")
    public Result<User> getPublicProfile(@PathVariable Long id) {
        Long viewerId = null;
        User currentUser = UserContext.get();
        if (currentUser != null) {
            viewerId = currentUser.getId();
        }
        return userService.getPublicProfile(id, viewerId);
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody User user) {
        Long userId = UserContext.get().getId();
        return userService.updateProfile(userId, user);
    }

    @PutMapping("/avatar")
    public Result<User> updateAvatar(@RequestBody Map<String, String> body) {
        Long userId = UserContext.get().getId();
        String avatar = body.get("avatar");
        return userService.updateAvatar(userId, avatar);
    }

    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody Map<String, String> body) {
        Long userId = UserContext.get().getId();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        return userService.updatePassword(userId, oldPassword, newPassword);
    }
}
