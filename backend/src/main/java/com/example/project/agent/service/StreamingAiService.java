package com.example.project.agent.service;

import com.example.project.agent.controller.StreamingAiController;
import com.example.project.agent.dto.PostGenerateRequest;
import com.example.project.agent.dto.PostGenerateResponse;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流式 AI 服务
 * <p>
 * 提供流式生成帖子和流式对话功能，使用 SSE 实时推送内容
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Service
public class StreamingAiService {

    private static final Logger logger = LoggerFactory.getLogger(StreamingAiService.class);

    @Autowired
    private StreamingChatLanguageModel streamingChatModel;

    /**
     * 流式生成帖子
     * <p>
     * 使用 SSE 实时推送生成进度
     * </p>
     *
     * @param request   帖子生成请求
     * @param requestId 请求ID
     * @param emitter   SSE emitter
     */
    @Async("taskExecutor")
    public void streamGeneratePost(PostGenerateRequest request, String requestId, SseEmitter emitter) {
        logger.info("Starting stream generate post: requestId={}", requestId);
        long startTime = System.currentTimeMillis();

        try {
            // 发送开始事件
            StreamingAiController.sendEvent(emitter, "start", Map.of(
                    "requestId", requestId,
                    "message", "开始生成帖子..."
            ));

            // 构建提示词
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(request);

            // 发送提示词事件
            StreamingAiController.sendEvent(emitter, "prompt", Map.of(
                    "systemPrompt", systemPrompt,
                    "userPrompt", userPrompt
            ));

            // 调用流式生成
            generateWithStreaming(emitter, systemPrompt, userPrompt);

            long endTime = System.currentTimeMillis();
            logger.info("Stream generate post completed: requestId={}, time={}ms", requestId, endTime - startTime);

        } catch (Exception e) {
            logger.error("Stream generate post failed: requestId={}", requestId, e);
            StreamingAiController.sendError(emitter, "生成帖子失败: " + e.getMessage());
        }
    }

    /**
     * 流式对话
     * <p>
     * 使用 SSE 实时推送 AI 回复
     * </p>
     *
     * @param message   用户消息
     * @param requestId 请求ID
     * @param emitter   SSE emitter
     */
    @Async("taskExecutor")
    public void streamChat(String message, String requestId, SseEmitter emitter) {
        logger.info("Starting stream chat: requestId={}", requestId);

        try {
            // 发送开始事件
            StreamingAiController.sendEvent(emitter, "start", Map.of(
                    "requestId", requestId,
                    "message", "思考中..."
            ));

            String systemPrompt = "你是一个友好的AI助手，请简洁明了地回答用户问题。";
            generateWithStreaming(emitter, systemPrompt, message);

        } catch (Exception e) {
            logger.error("Stream chat failed: requestId={}", requestId, e);
            StreamingAiController.sendError(emitter, "对话失败: " + e.getMessage());
        }
    }

    /**
     * 生成内容并实时推送流式响应
     *
     * @param emitter      SSE emitter
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     */
    private void generateWithStreaming(SseEmitter emitter, String systemPrompt, String userPrompt) {
        // 发送开始生成事件
        StreamingAiController.sendEvent(emitter, "generating", Map.of(
                "message", "AI正在生成内容...",
                "progress", 0
        ));

        StringBuilder accumulated = new StringBuilder();
        AtomicInteger charCount = new AtomicInteger(0);
        int estimatedTotal = 500; // 预估总字符数

        // 创建消息列表
        SystemMessage systemMessage = SystemMessage.from(systemPrompt);
        UserMessage userMessage = UserMessage.from(userPrompt);

        // 使用流式响应处理器
        streamingChatModel.generate(
            java.util.Arrays.asList(systemMessage, userMessage),
            new dev.langchain4j.model.StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    // 每个token生成时立即推送
                    accumulated.append(token);
                    int currentCount = charCount.addAndGet(token.length());
                    int progress = Math.min((int) ((currentCount * 100.0) / estimatedTotal), 95);
                    
                    StreamingAiController.sendEvent(emitter, "token", Map.of(
                            "token", token,
                            "accumulated", accumulated.toString(),
                            "progress", progress,
                            "charCount", currentCount
                    ));
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    // 流式生成完成后发送完成事件
                    int totalChars = accumulated.length();
                    StreamingAiController.sendEvent(emitter, "done", Map.of(
                            "content", accumulated.toString(),
                            "totalChars", totalChars,
                            "progress", 100
                    ));
                    logger.info("流式生成完成，总字符数: {}", totalChars);
                }

                @Override
                public void onError(Throwable error) {
                    logger.error("流式生成出错: {}", error.getMessage(), error);
                    StreamingAiController.sendError(emitter, "生成失败: " + error.getMessage());
                }
            }
        );
    }

    /**
     * 构建系统提示词
     *
     * @return 系统提示词
     */
    private String buildSystemPrompt() {
        return "你是一个专业的社交媒体内容创作助手。" +
                "请根据用户提供的主题和要求，生成一篇吸引人的社交媒体帖子。" +
                "帖子应该包含：标题、正文内容、相关标签。" +
                "请用中文回复，格式清晰易读。";
    }

    /**
     * 构建用户提示词
     *
     * @param request 帖子生成请求
     * @return 用户提示词
     */
    private String buildUserPrompt(PostGenerateRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("主题：").append(request.getTopic()).append("\n");

        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            prompt.append("分类：").append(request.getCategory()).append("\n");
        }
        if (request.getStyle() != null && !request.getStyle().isEmpty()) {
            prompt.append("风格：").append(request.getStyle()).append("\n");
        }
        if (request.getRequirements() != null && !request.getRequirements().isEmpty()) {
            prompt.append("要求：").append(request.getRequirements()).append("\n");
        }

        prompt.append("\n请生成一篇帖子，包含标题、正文和标签。");

        return prompt.toString();
    }

    /**
     * 解析生成的内容
     *
     * @param content AI生成的内容
     * @return 帖子生成响应
     */
    private PostGenerateResponse parseGeneratedContent(String content) {
        // 简单解析，提取标题、内容和标签
        String title = extractSection(content, "标题", "【标题】");
        String body = extractSection(content, "正文", "【正文】");
        String tags = extractSection(content, "标签", "【标签】");

        if (title.isEmpty()) {
            // 如果没有明确标记，使用第一行作为标题
            String[] lines = content.split("\n", 2);
            title = lines[0].trim();
            body = lines.length > 1 ? lines[1].trim() : content;
        }

        return PostGenerateResponse.builder()
                .title(title)
                .content(body.isEmpty() ? content : body)
                .tags(tags)
                .build();
    }

    /**
     * 提取章节内容
     *
     * @param content 完整内容
     * @param keyword 关键词
     * @param marker  标记
     * @return 提取的内容
     */
    private String extractSection(String content, String keyword, String marker) {
        int start = content.indexOf(marker);
        if (start == -1) {
            start = content.indexOf(keyword + "：");
            if (start == -1) {
                start = content.indexOf(keyword + ":");
            }
        }

        if (start == -1) {
            return "";
        }

        start = content.indexOf("：", start);
        if (start == -1) {
            start = content.indexOf(":", start);
        }
        if (start == -1) {
            return "";
        }
        start++;

        int end = content.length();
        String[] nextMarkers = {"【", "标题", "正文", "标签", "\n\n"};
        for (String nextMarker : nextMarkers) {
            int nextPos = content.indexOf(nextMarker, start);
            if (nextPos != -1 && nextPos < end) {
                end = nextPos;
            }
        }

        return content.substring(start, end).trim();
    }
}
