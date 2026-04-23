package com.example.project.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.project.common.CategoryConstants;
import com.example.project.dto.AiPostGenerateRequest;
import com.example.project.dto.AiPostGenerateResponse;
import com.example.project.entity.Post;
import com.example.project.service.AiPostService;
import com.example.project.service.PostService;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;

/**
 * AI 发帖服务实现类
 * 
 * ============================================================
 *                    AI Agent 开发步骤
 * ============================================================
 * 
 * 第一步：配置 AI 模型（已在 LangChain4jConfig 中完成）
 *   - ChatLanguageModel：同步模型，全局注入
 *   - StreamingChatLanguageModel：流式模型，全局注入
 * 
 * 第二步：设计提示词（Prompt Engineering）
 *   - 明确 AI 的角色和任务
 *   - 提供清晰的输入和输出格式
 *   - 添加约束条件和示例
 * 
 * 第三步：调用 AI 模型
 *   - 同步调用：chatLanguageModel.generate(prompt)
 *   - 流式调用：streamingChatModel.generate(prompt, handler)
 * 
 * 第四步：解析 AI 响应
 *   - 提取 JSON 或文本内容
 *   - 处理异常情况（fallback）
 * 
 * 第五步：业务集成
 *   - 将 AI 生成的内容保存到数据库
 *   - 或返回给前端展示
 * 
 * ============================================================
 */
@Service
public class AiPostServiceImpl implements AiPostService {

    private static final Logger logger = LoggerFactory.getLogger(AiPostServiceImpl.class);

    /**
     * 同步模型：全局注入，直接使用
     * 
     * 配置一次，到处使用！不需要每个地方都配置。
     */
    private final ChatLanguageModel chatLanguageModel;
    
    /**
     * 流式模型：全局注入，直接使用
     * 
     * 配置一次，到处使用！不需要每个地方都配置。
     * 后续可用于流式输出场景。
     */
    private final StreamingChatLanguageModel streamingChatModel;
    
    private final PostService postService;

    /**
     * 构造函数注入
     * 
     * Spring 会自动注入全局配置的 Bean
     */
    public AiPostServiceImpl(
            ChatLanguageModel chatLanguageModel,
            StreamingChatLanguageModel streamingChatModel,
            PostService postService
    ) {
        this.chatLanguageModel = chatLanguageModel;
        this.streamingChatModel = streamingChatModel;
        this.postService = postService;
    }

    // ==================== 同步生成 ====================

    @Override
    public AiPostGenerateResponse generatePost(AiPostGenerateRequest request) {
        logger.info("Generating AI post - topic: {}", request.getTopic());
        
        String prompt = buildPrompt(request);
        String generatedContent = chatLanguageModel.generate(prompt);
        
        return parseGeneratedContent(generatedContent, request);
    }

    @Override
    public AiPostGenerateResponse generateAndPublishPost(AiPostGenerateRequest request) {
        AiPostGenerateResponse response = generatePost(request);
        
        Post post = new Post();
        post.setTitle(response.getTitle());
        post.setContent(response.getContent());
        post.setCategory(response.getCategory());
        post.setDestination(response.getDestination());
        
        boolean created = postService.createPost(post);
        
        if (created) {
            response.setPostId(post.getId());
            logger.info("Successfully published AI post - id: {}", post.getId());
        }
        
        return response;
    }

    // ==================== 私有方法 ====================

    /**
     * 构建提示词（Prompt Engineering）
     * 
     * 提示词结构：
     * 1. 角色设定
     * 2. 任务描述
     * 3. 输入信息
     * 4. 输出格式
     * 5. 约束条件
     */
    private String buildPrompt(AiPostGenerateRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        // 角色设定
        prompt.append("你是一个大学生活动策划助手。请根据以下要求生成一篇帖子。\n\n");
        
        // 输入信息
        prompt.append("【要求】\n");
        prompt.append("- 主题：").append(request.getTopic()).append("\n");
        
        if (StringUtils.hasText(request.getCategory())) {
            prompt.append("- 分类：").append(request.getCategory()).append("\n");
        }
        if (StringUtils.hasText(request.getDestination())) {
            prompt.append("- 目的地：").append(request.getDestination()).append("\n");
        }
        if (StringUtils.hasText(request.getStyle())) {
            prompt.append("- 风格：").append(request.getStyle()).append("\n");
        }
        
        // 输出格式
        prompt.append("\n【格式要求】\n");
        prompt.append("请严格按照以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"帖子标题（20字以内）\",\n");
        prompt.append("  \"content\": \"帖子正文（200-500字）\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    /**
     * 解析 AI 生成的内容
     */
    private AiPostGenerateResponse parseGeneratedContent(String generatedContent, AiPostGenerateRequest request) {
        try {
            String jsonContent = extractJson(generatedContent);
            com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSON.parseObject(jsonContent);
            
            String title = json.getString("title");
            String content = json.getString("content");
            String category = StringUtils.hasText(request.getCategory()) 
                    ? request.getCategory() 
                    : CategoryConstants.OTHER;
            
            return AiPostGenerateResponse.builder()
                    .title(title)
                    .content(content)
                    .category(category)
                    .destination(request.getDestination())
                    .build();
                    
        } catch (Exception e) {
            logger.error("Failed to parse AI generated content", e);
            return createFallbackResponse(generatedContent, request);
        }
    }

    /**
     * 从文本中提取 JSON
     */
    private String extractJson(String content) {
        int startIndex = content.indexOf("{");
        int endIndex = content.lastIndexOf("}");
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return content.substring(startIndex, endIndex + 1);
        }
        
        throw new IllegalArgumentException("No valid JSON found in response");
    }

    /**
     * 创建降级响应（Fallback）
     */
    private AiPostGenerateResponse createFallbackResponse(String content, AiPostGenerateRequest request) {
        String title = StringUtils.hasText(request.getTopic()) 
                ? request.getTopic() 
                : "AI生成的帖子";
        String category = StringUtils.hasText(request.getCategory()) 
                ? request.getCategory() 
                : CategoryConstants.OTHER;
        
        return AiPostGenerateResponse.builder()
                .title(title)
                .content(content)
                .category(category)
                .destination(request.getDestination())
                .build();
    }
}
