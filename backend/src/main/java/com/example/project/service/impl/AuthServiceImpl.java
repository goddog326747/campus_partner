package com.example.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.project.common.Result;
import com.example.project.entity.User;
import com.example.project.mapper.UserMapper;
import com.example.project.service.AuthService;
import com.example.project.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<Map<String, Object>> login(String username, String password) {
        // 使用 Mapper 查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user != null && user.getPassword().equals(password)) {
            // 登录成功，生成 Token（包含用户ID）
            String token = JwtUtils.sign(username, user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);

            // 返回用户信息 (脱敏)
            user.setPassword(null);
            data.put("userInfo", user);

            return Result.success("登录成功", data);
        } else {
            return Result.error(401, "用户名或密码错误");
        }
    }
}
