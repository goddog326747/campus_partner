package com.example.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Elasticsearch 配置类
 * <p>
 * 启用 Elasticsearch 仓库和异步支持
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.project.elasticsearch.repository")
@EnableAsync
public class ElasticsearchConfig {
}
