package com.example.project.config;

import com.example.project.service.storage.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * <p>
 * 配置CORS跨域访问和静态资源处理
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private StorageProperties storageProperties;

    /**
     * 配置CORS跨域映射
     * <p>
     * 允许所有来源、所有方法的跨域请求
     * </p>
     *
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源处理器
     * <p>
     * 当使用本地存储时，配置文件访问路径映射
     * </p>
     *
     * @param registry 资源处理注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if ("local".equalsIgnoreCase(storageProperties.getType())) {
            String basePath = storageProperties.getLocal().getBasePath();
            String urlPrefix = storageProperties.getLocal().getUrlPrefix();
            
            registry.addResourceHandler(urlPrefix + "/**")
                    .addResourceLocations("file:" + basePath + "/");
        }
    }
}
