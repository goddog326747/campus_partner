package com.example.project.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * <p>
 * 配置MyBatis-Plus插件，包括分页插件等
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 创建MyBatis-Plus拦截器
     * <p>
     * 添加分页插件，支持MySQL数据库分页查询
     * </p>
     *
     * @return MyBatis-Plus拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
