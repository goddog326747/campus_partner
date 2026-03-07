package com.example.project.service;

import com.example.project.common.Result;
import java.util.Map;

public interface AuthService {
    Result<Map<String, Object>> login(String username, String password);
}
