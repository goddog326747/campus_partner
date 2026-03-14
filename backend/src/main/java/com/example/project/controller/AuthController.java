package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.entity.User;
import com.example.project.service.AuthService;
import com.example.project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

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

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        logger.info("User register attempt - username: {}", user.getUsername());
        try {
            Result<User> result = userService.register(user);
            if (result.getCode() == 200) {
                logger.info("User register successful - username: {}", user.getUsername());
            } else {
                logger.warn("User register failed - username: {}, reason: {}", user.getUsername(), result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("Error occurred during register process - username: {}", user.getUsername(), e);
            return Result.error(500, "注册过程发生错误");
        }
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        return authService.getCurrentUser();
    }
}
