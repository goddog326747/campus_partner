package com.example.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.project.assistant.PostAssistant;
import com.example.project.agent.PostTools;
import com.example.project.mapper.UserMapper;
import com.example.project.service.PostService;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;

/**
 * AI Agent 配置类
 * 
 * ============================================================
 *                    AI Agent 组装配置
 * ============================================================
 * 
 * 这个配置类负责组装 AI Agent，把所有组件连接起来。
 * 
 * AiServices 是 LangChain4j 的核心类，用于构建 AI Agent。
 * 
 * 组装流程：
 * 1. 设置 AI 模型（必须有）
 * 2. 注册工具类（可选，但这是 Tool Calling 的关键）
 * 3. 配置记忆（可选，用于多轮对话）
 * 4. 构建代理接口
 * 
 * ============================================================
 * 
 * 【关键概念】
 * 
 * AiServices.builder() - 构建器模式
 * ├── .chatLanguageModel() - 设置 AI 模型
 * ├── .tools() - 注册工具类（可以有多个）
 * ├── .chatMemoryProvider() - 提供对话记忆
 * └── .build() - 构建代理实例
 * 
 * 工具类注册方式：
 * .tools(new PostTools(postService, userMapper))  // 单个工具类
 * .tools(new Tool1(), new Tool2(), ...)            // 多个工具类
 * 
 * 记忆配置方式：
 * .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
 * // 每个 conversationId 对应一个独立的记忆，保留最近10条消息
 * 
 * ============================================================
 */
@Configuration
public class AiAgentConfig {

    /**
     * 创建帖子助手 Agent
     * 
     * 这个 Agent 具备以下能力：
     * 1. 生成帖子内容
     * 2. 查询用户风格（通过 Tool）
     * 3. 获取热门话题（通过 Tool）
     * 4. 检查敏感词（通过 Tool）
     * 
     * @param chatLanguageModel AI 模型（从 LangChain4jConfig 注入）
     * @param postService 帖子服务
     * @param userMapper 用户 Mapper
     * @return PostAssistant 实例
     */
    @Bean
    public PostAssistant postAssistant(
            ChatLanguageModel chatLanguageModel,
            PostService postService,
            UserMapper userMapper
    ) {
        return AiServices.builder(PostAssistant.class)
                // ========== 1. 设置 AI 模型 ==========
                // 这是必须的，决定了使用哪个 LLM
                .chatLanguageModel(chatLanguageModel)
                
                // ========== 2. 注册工具类 ==========
                // 这是 Tool Calling 的关键！
                // AI 会根据用户输入自动决定是否调用这些工具
                .tools(new PostTools(postService, userMapper))
                
                // ========== 3. 配置对话记忆 ==========
                // 让 AI 能记住之前的对话
                // 每个 conversationId 对应一个独立的记忆
                .chatMemoryProvider(conversationId -> 
                    MessageWindowChatMemory.withMaxMessages(10)
                )
                
                // ========== 4. 构建代理 ==========
                .build();
    }
}
