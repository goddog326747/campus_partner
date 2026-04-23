package com.example.project.service;

import com.example.project.dto.AiPostGenerateRequest;
import com.example.project.dto.AiPostGenerateResponse;

/**
 * AI帖子生成服务接口
 * <p>
 * 提供AI自动生成帖子内容的服务能力，支持生成草稿和直接发布
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface AiPostService {
    
    /**
     * 生成AI帖子内容（仅生成，不发布）
     *
     * @param request AI帖子生成请求，包含主题、分类等信息
     * @return 生成的帖子内容，包含标题、正文等
     */
    AiPostGenerateResponse generatePost(AiPostGenerateRequest request);
    
    /**
     * 生成并发布AI帖子
     * <p>
     * 调用AI服务生成帖子内容，并自动发布到平台
     * </p>
     *
     * @param request AI帖子生成请求，包含主题、分类等信息
     * @return 生成并发布的帖子信息
     */
    AiPostGenerateResponse generateAndPublishPost(AiPostGenerateRequest request);
}
