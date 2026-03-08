package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器，提供用户登录等认证相关的API接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     * @param user 包含用户名和密码的Map
     * @return 登录结果，包含token等信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");
        
        logger.info("User login attempt - username: {}", username);
        
        try {
            Result<Map<String, Object>> result = authService.login(username, password);
            if (result.getCode() == 200) {
                logger.info("User login successful - username: {}", username);
            } else {
                logger.warn("User login failed - username: {}, reason: {}", username, result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("Error occurred during login process - username: {}", username, e);
            return Result.error(500, "登录过程发生错误");
        }
    }
}
