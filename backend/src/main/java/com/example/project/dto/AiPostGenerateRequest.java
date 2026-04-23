package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI帖子生成请求DTO
 * <p>
 * 封装AI帖子生成接口的请求参数
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPostGenerateRequest {
    
    /** 帖子主题 */
    private String topic;
    
    /** 帖子分类 */
    private String category;
    
    /** 目的地 */
    private String destination;
    
    /** 写作风格 */
    private String style;
}
