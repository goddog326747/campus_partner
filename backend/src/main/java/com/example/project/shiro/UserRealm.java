package com.example.project.shiro;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.project.entity.User;
import com.example.project.mapper.UserMapper;
import com.example.project.util.JwtUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserRealm extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(UserRealm.class);

    @Autowired
    private UserMapper userMapper;

    /**
     * 必须重写此方法，否则Shiro会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权：获取用户权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = JwtUtils.getUsername(principals.toString());
        logger.debug("Performing authorization for user: {}", username);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 模拟：根据用户名查询权限
        // Set<String> roleSet = userService.getUserRoles(username);
        // Set<String> permissionSet = userService.getUserPermissions(username);
        // info.setRoles(roleSet);
        // info.setStringPermissions(permissionSet);
        return info;
    }

    /**
     * 认证：验证用户身份
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        String username = JwtUtils.getUsername(token);

        logger.info("Performing authentication for user: {}", username);

        if (username == null) {
            logger.warn("Invalid token - username could not be extracted");
            throw new AuthenticationException("token invalid");
        }

        // 查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            logger.warn("User not found in database: {}", username);
            throw new AuthenticationException("User didn't existed!");
        }

        if (!JwtUtils.verify(token)) {
            logger.warn("Token verification failed for user: {}", username);
            throw new AuthenticationException("Username or password error");
        }

        logger.info("Successfully authenticated user: {}", username);
        return new SimpleAuthenticationInfo(token, token, "my_realm");
    }
}
