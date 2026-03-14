package com.example.project.service;

import com.example.project.common.Result;
import com.example.project.entity.User;
import java.util.Map;

public interface AuthService {
    Result<Map<String, Object>> login(String username, String password);
    Result<User> getCurrentUser();
}
