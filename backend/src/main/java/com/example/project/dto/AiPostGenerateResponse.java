package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI帖子生成响应DTO
 * <p>
 * 封装AI帖子生成接口的响应数据
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPostGenerateResponse {
    
    /** 生成的帖子标题 */
    private String title;
    
    /** 生成的帖子内容 */
    private String content;
    
    /** 帖子分类 */
    private String category;
    
    /** 目的地 */
    private String destination;
    
    /** 发布成功后的帖子ID */
    private Long postId;
}
