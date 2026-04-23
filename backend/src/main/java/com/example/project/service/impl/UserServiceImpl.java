package com.example.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.project.common.Result;
import com.example.project.entity.User;
import com.example.project.mapper.UserMapper;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<User> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result<User> getPublicProfile(Long userId, Long viewerId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        user.setPassword(null);
        
        boolean isOwner = viewerId != null && viewerId.equals(userId);
        
        if (!isOwner) {
            Integer privacyProfile = user.getPrivacyProfile();
            if (privacyProfile == null) {
                privacyProfile = 0;
            }
            
            if (privacyProfile == 2) {
                User publicUser = new User();
                publicUser.setId(user.getId());
                publicUser.setNickname(user.getNickname());
                publicUser.setAvatar(user.getAvatar());
                publicUser.setBio(user.getBio());
                return Result.success(publicUser);
            }
            
            Integer privacyContact = user.getPrivacyContact();
            if (privacyContact == null) {
                privacyContact = 1;
            }
            
            if (privacyContact >= 1) {
                user.setPhone(null);
                user.setEmail(null);
                user.setWechat(null);
                user.setQq(null);
            }
        }
        
        return Result.success(user);
    }

    @Override
    public Result<User> updateProfile(Long id, User user) {
        User existUser = userMapper.selectById(id);
        if (existUser == null) {
            return Result.error(404, "用户不存在");
        }
        
        user.setId(id);
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.updateById(user);
        
        User updatedUser = userMapper.selectById(id);
        updatedUser.setPassword(null);
        return Result.success("更新成功", updatedUser);
    }

    @Override
    public Result<User> updateAvatar(Long id, String avatar) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id)
               .set(User::getAvatar, avatar)
               .set(User::getUpdateTime, LocalDateTime.now());
        userMapper.update(null, wrapper);
        
        User updatedUser = userMapper.selectById(id);
        updatedUser.setPassword(null);
        return Result.success("头像更新成功", updatedUser);
    }

    @Override
    public Result<String> updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error(400, "原密码错误");
        }
        
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id)
               .set(User::getPassword, newPassword)
               .set(User::getUpdateTime, LocalDateTime.now());
        userMapper.update(null, wrapper);
        
        return Result.success("密码修改成功", null);
    }

    @Override
    public Result register(User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            return Result.error(400, "用户名和密码不能为空");
        }
        
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            return Result.error(400, "用户名已存在");
        }
        
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        user.setStatus(1);
        user.setPrivacyProfile(0);
        user.setPrivacyContact(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        user.setPassword(null);
        return Result.success("注册成功", user);
    }
}
