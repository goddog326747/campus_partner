package com.example.project.service.impl;

import com.example.project.common.Result;
import com.example.project.dto.vo.LoginResponseVO;
import com.example.project.entity.User;
import com.example.project.mapper.UserMapper;
import com.example.project.service.AuthService;
import com.example.project.shiro.util.JwtUtils;
import com.example.project.shiro.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<LoginResponseVO> login(String username, String password) {
        User user = userMapper.selectByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            String token = JwtUtils.sign(username, user.getId());

            user.setPassword(null);
            LoginResponseVO response = LoginResponseVO.builder()
                    .token(token)
                    .userInfo(user)
                    .build();

            return Result.success("登录成功", response);
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
