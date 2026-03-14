package com.example.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.project.common.Result;
import com.example.project.entity.User;
import com.example.project.mapper.UserMapper;
import com.example.project.service.AuthService;
import com.example.project.util.JwtUtils;
import com.example.project.util.UserContext;
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
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user != null && user.getPassword().equals(password)) {
            String token = JwtUtils.sign(username, user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);

            user.setPassword(null);
            data.put("userInfo", user);

            return Result.success("登录成功", data);
        } else {
            return Result.error(401, "用户名或密码错误");
        }
    }

    @Override
    public Result<User> getCurrentUser() {
        User user = UserContext.get();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        User dbUser = userMapper.selectById(user.getId());
        if (dbUser == null) {
            return Result.error(404, "用户不存在");
        }
        dbUser.setPassword(null);
        return Result.success(dbUser);
    }
}
