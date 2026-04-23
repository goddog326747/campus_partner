package com.example.project.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.common.CategoryConstants;
import com.example.project.common.Result;
import com.example.project.dto.AiPostGenerateRequest;
import com.example.project.dto.AiPostGenerateResponse;
import com.example.project.dto.vo.CategoryListVO;
import com.example.project.service.AiPostService;

import java.util.Arrays;

/**
 * AI帖子生成控制器
 * <p>
 * 提供AI自动生成帖子内容的API接口，支持生成帖子草稿和直接发布
 * </p>
 *
 * @author system
 * @since 1.0
 */
@RestController
@RequestMapping("/api/ai/post")
public class AiPostController {

    private static final Logger logger = LoggerFactory.getLogger(AiPostController.class);

    private final AiPostService aiPostService;

    @Autowired
    public AiPostController(AiPostService aiPostService) {
        this.aiPostService = aiPostService;
    }

    /**
     * 生成AI帖子（仅生成，不发布）
     *
     * @param request AI帖子生成请求，包含主题、分类等信息
     * @return 生成的帖子内容，包含标题、正文等
     */
    @PostMapping("/generate")
    public Result<AiPostGenerateResponse> generatePost(@RequestBody AiPostGenerateRequest request) {
        logger.info("Received AI post generation request - topic: {}", request.getTopic());
        
        try {
            AiPostGenerateResponse response = aiPostService.generatePost(request);
            return Result.success(response);
        } catch (Exception e) {
            logger.error("Failed to generate AI post", e);
            return Result.error(500, "AI生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成并发布AI帖子
     * <p>
     * 调用AI服务生成帖子内容，并自动发布到平台
     * </p>
     *
     * @param request AI帖子生成请求，包含主题、分类等信息
     * @return 生成并发布的帖子信息
     */
    @PostMapping("/publish")
    public Result<AiPostGenerateResponse> generateAndPublishPost(@RequestBody AiPostGenerateRequest request) {
        logger.info("Received AI post generation and publish request - topic: {}", request.getTopic());
        
        try {
            AiPostGenerateResponse response = aiPostService.generateAndPublishPost(request);
            return Result.success(response);
        } catch (Exception e) {
            logger.error("Failed to generate and publish AI post", e);
            return Result.error(500, "AI生成发布失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有可用的帖子分类列表
     *
     * @return 分类列表及其描述信息
     */
    @GetMapping("/categories")
    public Result<CategoryListVO> getCategories() {
        CategoryListVO response = CategoryListVO.builder()
                .categories(Arrays.asList(CategoryConstants.ALL_CATEGORIES))
                .description("可选的帖子分类列表")
                .build();
        return Result.success(response);
    }
}
