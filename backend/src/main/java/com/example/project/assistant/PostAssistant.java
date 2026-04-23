package com.example.project.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.SystemMessage;

/**
 * 帖子助手 Agent 接口
 * 
 * ============================================================
 *                    AI Agent 接口定义
 * ============================================================
 * 
 * 这个接口定义了 AI Agent 的能力。
 * LangChain4j 会自动实现这个接口。
 * 
 * @SystemMessage：系统提示词，定义 AI 的角色和行为
 * @UserMessage：用户消息
 * @MemoryId：会话 ID，用于区分不同用户的对话记忆
 * 
 * ============================================================
 */
public interface PostAssistant {
    
    /**
     * 与 AI 助手对话
     * 
     * AI 可以：
     * - 生成帖子内容
     * - 查询用户风格（自动调用 Tool）
     * - 获取热门话题（自动调用 Tool）
     * - 检查敏感词（自动调用 Tool）
     * 
     * @param conversationId 会话 ID（用于记忆）
     * @param userMessage 用户消息
     * @return AI 回复
     */
    @SystemMessage("""
        你是一个专业的帖子生成助手。
        你可以帮助用户生成各类活动帖子。
        
        当用户要求生成帖子时：
        1. 如果用户提到"我的风格"，先调用 getUserPostStyle 工具查询
        2. 可以调用 getHotTopics 获取热门话题作为参考
        3. 生成内容后，调用 checkSensitiveContent 检查敏感词
        
        请用友好、专业的语气回复用户。
        """)
    String chat(@MemoryId String conversationId, @UserMessage String userMessage);
}
