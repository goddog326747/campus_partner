package com.example.project.config;

import com.example.project.shiro.JwtFilter;
import com.example.project.shiro.UserRealm;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro安全配置类
 * <p>
 * 配置Apache Shiro安全框架，实现JWT令牌认证和无状态会话管理
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Configuration
public class ShiroConfig {

    /**
     * 创建安全管理器
     * <p>
     * 配置自定义Realm和关闭Session存储，实现无状态认证
     * </p>
     *
     * @param realm 自定义用户Realm
     * @return 安全管理器实例
     */
    @Bean("securityManager")
    public DefaultWebSecurityManager getManager(UserRealm realm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        // 使用自定义Realm
        manager.setRealm(realm);

        /*
         * 关闭 Shiro 自带的 Session，详情见文档
         * http://shiro.apache.org/session-management.html#SessionManagement-StatelessApplications%28Sessionless%29
         */
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        manager.setSubjectDAO(subjectDAO);

        return manager;
    }

    /**
     * 创建Shiro过滤器工厂Bean
     * <p>
     * 配置URL过滤规则，定义哪些接口需要认证，哪些可以匿名访问
     * </p>
     *
     * @param securityManager 安全管理器
     * @return Shiro过滤器工厂Bean
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean factory(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        // 添加自己的过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", new JwtFilter());
        factoryBean.setFilters(filterMap);
        factoryBean.setSecurityManager(securityManager);

        /*
         * 自定义url规则
         * http://shiro.apache.org/web.html#urls-
         */
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        // 所有请求通过我们自己的JWT Filter
        // 登录接口允许匿名访问
        filterRuleMap.put("/api/auth/**", "anon");
        // 帖子列表、分类、评论列表接口允许匿名访问
        filterRuleMap.put("/api/post/list", "anon");
        filterRuleMap.put("/api/post/categories", "anon");
        filterRuleMap.put("/api/comment/list", "anon"); // Allow viewing comments without login
        filterRuleMap.put("/api/comment/replies/**", "anon"); // Allow viewing replies without login
        // AI 助手接口允许匿名访问
        filterRuleMap.put("/api/ai/**", "anon");
        // 帖子创建和评论操作需要通过 jwt 过滤器
        filterRuleMap.put("/api/post/create", "jwt");
        filterRuleMap.put("/api/comment/**", "jwt"); // Comment operations require login
        // 其他接口需要通过 jwt 过滤器
        filterRuleMap.put("/**", "jwt");
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);
        return factoryBean;
    }
}
