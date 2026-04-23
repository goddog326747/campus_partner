package com.example.project.agent.dto;

import lombok.Data;

/**
 * AI 对话请求
 */
@Data
public class ChatRequest {
    
    /**
     * 用户输入的消息
     */
    private String message;
    
    /**
     * 会话 ID（用于保持对话上下文）
     */
    private String conversationId;
    
    /**
     * 对话模式：SIMPLE（简单对话）、REACT（ReAct 模式）
     */
    private String mode = "SIMPLE";
}
