package com.example.project.agent.dto;

import lombok.Builder;
import lombok.Data;

/**
 * AI 帖子生成响应
 */
@Data
@Builder
public class PostGenerateResponse {
    
    /**
     * 生成的帖子标题
     */
    private String title;
    
    /**
     * 生成的帖子内容
     */
    private String content;
    
    /**
     * 帖子分类
     */
    private String category;
    
    /**
     * 执行 ID
     */
    private String executionId;
    
    /**
     * 执行耗时（毫秒）
     */
    private long executionTime;
    
    /**
     * 执行的节点数量
     */
    private int nodesExecuted;
    
    /**
     * 是否已发布
     */
    private boolean published;
    
    /**
     * 发布的帖子 ID（如果已发布）
     */
    private Long postId;
    
    /**
     * 生成的标签
     */
    private String tags;
}
