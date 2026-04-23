package com.example.project.agent.dto;

import lombok.Data;

/**
 * AI 帖子生成请求
 */
@Data
public class PostGenerateRequest {
    
    /**
     * 帖子主题
     */
    private String topic;
    
    /**
     * 帖子分类
     */
    private String category;
    
    /**
     * 风格要求：活泼、正式、幽默等
     */
    private String style;
    
    /**
     * 额外要求
     */
    private String requirements;
    
    /**
     * 是否直接发布
     */
    private boolean publish = false;
}
