package com.example.project.shiro;

import com.example.project.entity.User;
import com.example.project.util.JwtUtils;
import com.example.project.util.UserContext;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT过滤器，拦截请求并进行Token校验
 */
public class JwtFilter extends BasicHttpAuthenticationFilter {

    /**
     * 判断是否允许访问
     * 如果带有 Authorization Header，则进行登录验证
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginAttempt(request, response)) {
            try {
                executeLogin(request, response);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        // 如果没有携带 Token，且是无需登录的接口，可以放行；如果是需登录接口，Shiro 会在后续拦截
        // 这里简单处理：允许匿名访问（由 ShiroConfig 配置拦截规则），或者拒绝
        return true;
    }

    /**
     * 判断请求头是否带有 Authorization
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authHeader = req.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ");
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
        // 注意：实际项目中，应该从 Redis 或数据库获取完整的 User 对象
        String username = JwtUtils.getUsername(token);
        User user = new User();
        user.setUsername(username);
        UserContext.set(user);
        
        return true;
    }

    /**
     * 请求处理完成后，清除 ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception {
        UserContext.remove();
        super.afterCompletion(request, response, exception);
    }

    /**
     * 处理跨域请求 (CORS)
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        
        // 跨域时会首先发送一个 option 请求，这里我们给 option 请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}
