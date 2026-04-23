package com.example.project.service;

import com.example.project.common.Result;
import com.example.project.dto.vo.LoginResponseVO;
import com.example.project.entity.User;

/**
 * 认证服务接口
 * <p>
 * 提供用户认证相关的服务，包括登录验证和获取当前用户信息
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface AuthService {

    /**
     * 用户登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，成功时返回用户信息和token
     */
    Result<LoginResponseVO> login(String username, String password);
    
    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    Result<User> getCurrentUser();
}
