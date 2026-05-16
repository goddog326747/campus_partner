package com.example.project.agent.dto;

import lombok.Data;

/**
 * AI 帖子生成请求
 */
@Data
public class PostGenerateRequest {
    
    private String topic;
    
    private String category;
    
    private String style;
    
    private String requirements;
    
    private String conversationId;
    
    private boolean publish = false;
}
