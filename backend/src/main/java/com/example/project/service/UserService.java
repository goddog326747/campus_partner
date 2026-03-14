package com.example.project.service;

import com.example.project.common.Result;
import com.example.project.entity.User;

public interface UserService {
    Result<User> getUserById(Long id);
    Result<User> getPublicProfile(Long userId, Long viewerId);
    Result<User> updateProfile(Long id, User user);
    Result<User> updateAvatar(Long id, String avatar);
    Result<String> updatePassword(Long id, String oldPassword, String newPassword);
    Result register(User user);
}
