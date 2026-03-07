package com.example.project.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 自定义 JWT Token，实现 Shiro 的 AuthenticationToken 接口
 */
public class JwtToken implements AuthenticationToken {

    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
