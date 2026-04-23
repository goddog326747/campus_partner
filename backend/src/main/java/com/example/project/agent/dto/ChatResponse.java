package com.example.project.agent.dto;

import lombok.Builder;
import lombok.Data;

/**
 * AI 对话响应
 */
@Data
@Builder
public class ChatResponse {
    
    /**
     * AI 回复内容
     */
    private String reply;
    
    /**
     * 执行 ID（用于追踪）
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
}
