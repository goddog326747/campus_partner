package com.example.project.service;

import com.example.project.common.Result;
import com.example.project.entity.User;

/**
 * 用户服务接口
 * <p>
 * 提供用户信息管理的服务，包括用户资料查询、更新和注册等功能
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface UserService {
    
    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Result<User> getUserById(Long id);
    
    /**
     * 获取用户的公开资料
     *
     * @param userId   被查看用户的ID
     * @param viewerId 查看者用户ID，可为null
     * @return 用户公开资料信息
     */
    Result<User> getPublicProfile(Long userId, Long viewerId);
    
    /**
     * 更新用户资料
     *
     * @param id   用户ID
     * @param user 包含更新信息的用户对象
     * @return 更新后的用户信息
     */
    Result<User> updateProfile(Long id, User user);
    
    /**
     * 更新用户头像
     *
     * @param id     用户ID
     * @param avatar 新头像URL
     * @return 更新后的用户信息
     */
    Result<User> updateAvatar(Long id, String avatar);
    
    /**
     * 修改用户密码
     *
     * @param id          用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 操作结果
     */
    Result<String> updatePassword(Long id, String oldPassword, String newPassword);
    
    /**
     * 用户注册
     *
     * @param user 注册用户信息
     * @return 注册结果，成功时返回用户信息
     */
    Result register(User user);
}
