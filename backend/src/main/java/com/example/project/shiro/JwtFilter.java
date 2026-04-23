package com.example.project.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.project.entity.User;
import com.example.project.shiro.util.JwtUtils;
import com.example.project.shiro.util.UserContext;

/**
 * JWT过滤器，拦截请求并进行Token校验
 */
public class JwtFilter extends BasicHttpAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    /**
     * 判断是否允许访问
     * 如果带有 Authorization Header，则进行登录验证
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginAttempt(request, response)) {
            try {
                logger.debug("Attempting to authenticate request with token");
                executeLogin(request, response);
                return true;
            } catch (Exception e) {
                logger.error("Authentication failed", e);
                return false;
            }
        }
        // 如果没有携带 Token，且是无需登录的接口，可以放行；如果是需登录接口，Shiro 会在后续拦截
        // 这里简单处理：允许匿名访问（由 ShiroConfig 配置拦截规则），或者拒绝
        logger.debug("No token found, allowing access based on Shiro config");
        return true;
    }

    /**
     * 判断请求头是否带有 Authorization
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authHeader = req.getHeader("Authorization");
        boolean isLoginAttempt = authHeader != null && authHeader.startsWith("Bearer ");
        if (isLoginAttempt) {
            logger.debug("Found Authorization header, attempting login");
        }
        return isLoginAttempt;
    }

    /**
     * 执行登录
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authHeader = httpServletRequest.getHeader("Authorization");
        // 去掉 "Bearer " 前缀
        String token = authHeader.substring(7);
        JwtToken jwtToken = new JwtToken(token);
        // 提交给 Realm 进行登入
        getSubject(request, response).login(jwtToken);
        
        // 登录成功后，将用户信息存入 ThreadLocal
        String username = JwtUtils.getUsername(token);
        Long userId = JwtUtils.getUserId(token);
        User user = new User();
        user.setUsername(username);
        user.setId(userId);
        UserContext.set(user);

        logger.debug("Successfully authenticated user: {}, userId: {}", username, userId);
        return true;
    }

    /**
     * 请求处理完成后，清除 ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception {
        UserContext.remove();
        if (exception != null) {
            logger.error("Error occurred during request processing", exception);
        }
        super.afterCompletion(request, response, exception);
    }
}
