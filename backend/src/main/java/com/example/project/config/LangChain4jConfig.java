package com.example.project.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 配置类
 * 
 * ============================================================
 *                 AI Agent 开发 - 第一步：配置模型
 * ============================================================
 * 
 * 这个配置类负责创建 AI 模型的 Bean，供其他服务注入使用。
 * 
 * 这里配置了两种模型：
 * 
 * 1. ChatLanguageModel（同步模型）
 *    - 一次性返回完整结果
 *    - 适合简单场景、后台任务
 * 
 * 2. StreamingChatLanguageModel（流式模型）【全局可用】
 *    - 逐 token 返回结果
 *    - 适合需要实时反馈的场景
 *    - 注入后直接使用，无需重复配置
 * 
 * ============================================================
 */
@Configuration
public class LangChain4jConfig {
    
    @Value("${aliyun.ai.api-key}")
    private String apiKey;
    
    @Value("${aliyun.ai.api-url}")
    private String apiUrl;
    
    @Value("${aliyun.ai.model}")
    private String model;
    
    /**
     * 创建同步聊天模型 Bean（全局可用）
     * 
     * 使用方式：直接注入
     * 
     * @Autowired
     * private ChatLanguageModel chatLanguageModel;
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(extractBaseUrl(apiUrl))
                .modelName(model)
                .timeout(Duration.ofSeconds(60))
                .temperature(0.7)
                .maxTokens(2000)
                .build();
    }
    
    /**
     * 创建流式聊天模型 Bean（全局可用）
     * 
     * ============================================================
     *                    全局流式配置
     * ============================================================
     * 
     * 配置一次，全局可用！
     * 
     * 使用方式：
     * 
     * @Autowired
     * private StreamingChatLanguageModel streamingChatModel;
     * 
     * // 直接使用
     * streamingChatModel.generate(prompt, handler);
     * 
     * 或者返回 Flux：
     * Flux<String> stream = Flux.create(emitter -> {
     *     streamingChatModel.generate(prompt, new StreamingResponseHandler<>() {
     *         @Override
     *         public void onNext(String token) {
     *             emitter.next(token);
     *         }
     *         @Override
     *         public void onComplete(Response<AiMessage> response) {
     *             emitter.complete();
     *         }
     *     });
     * });
     * 
     * ============================================================
     * 
     * @return StreamingChatLanguageModel 实例
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(extractBaseUrl(apiUrl))
                .modelName(model)
                .timeout(Duration.ofSeconds(60))
                .temperature(0.7)
                .maxTokens(2000)
                .build();
    }
    
    private String extractBaseUrl(String apiUrl) {
        if (apiUrl.contains("/chat/completions")) {
            return apiUrl.replace("/chat/completions", "");
        }
        return apiUrl;
    }
}
