package com.example.project.agent.service;

import com.example.project.agent.dto.PostGenerateRequest;
import com.example.project.agent.flow.sse.SseHelper;
import com.example.project.agent.service.impl.PostContentParser;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StreamingAiService {

    private static final Logger logger = LoggerFactory.getLogger(StreamingAiService.class);

    @Autowired
    @Qualifier("flashStreamingModel")
    private StreamingChatLanguageModel streamingChatModel;

    @Async("taskExecutor")
    public void streamGeneratePost(PostGenerateRequest request, String requestId, SseEmitter emitter) {
        logger.info("Starting stream generate post: requestId={}", requestId);
        long startTime = System.currentTimeMillis();

        try {
            SseHelper.sendEvent(emitter, "start", Map.of(
                    "requestId", requestId,
                    "message", "开始生成帖子..."
            ));

            // 构建提示词
            String systemPrompt = "你是一个专业的社交媒体内容创作助手。请根据用户提供的主题、分类、风格和要求，生成一篇吸引人的帖子。\n" +
                    "输出格式要求：\n" +
                    "【标题】xxx\n" +
                    "【正文】xxx\n" +
                    "【标签】xxx, xxx, xxx\n" +
                    "注意：标题要吸引人，正文要生动有趣，标签用逗号分隔。";

            String userPrompt = String.format("主题：%s\n分类：%s\n风格：%s\n补充要求：%s",
                    request.getTopic() != null ? request.getTopic() : "",
                    request.getCategory() != null ? request.getCategory() : "",
                    request.getStyle() != null ? request.getStyle() : "",
                    request.getRequirements() != null ? request.getRequirements() : ""
            );

            // 使用真正的流式生成
            generateWithStreaming(emitter, systemPrompt, userPrompt);

            long endTime = System.currentTimeMillis();
            logger.info("Stream generate post completed: requestId={}, time={}ms", requestId, endTime - startTime);

        } catch (Exception e) {
            logger.error("Stream generate post failed: requestId={}", requestId, e);
            SseHelper.sendError(emitter, "生成帖子失败: " + e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void streamChat(String message, String requestId, SseEmitter emitter) {
        logger.info("Starting stream chat: requestId={}", requestId);

        try {
            SseHelper.sendEvent(emitter, "start", Map.of(
                    "requestId", requestId,
                    "message", "思考中..."
            ));

            String systemPrompt = "你是一个友好的AI助手，请简洁明了地回答用户问题。";
            generateWithStreaming(emitter, systemPrompt, message);

        } catch (Exception e) {
            logger.error("Stream chat failed: requestId={}", requestId, e);
            SseHelper.sendError(emitter, "对话失败: " + e.getMessage());
        }
    }

    private void generateWithStreaming(SseEmitter emitter, String systemPrompt, String userPrompt) {
        SystemMessage systemMessage = SystemMessage.from(systemPrompt);
        UserMessage userMessage = UserMessage.from(userPrompt);

        List messages = Arrays.asList(systemMessage, userMessage);
        streamingChatModel.generate(messages, new PostStreamingChatHandler(emitter));
    }

    private static class StreamingChatHandler implements dev.langchain4j.model.StreamingResponseHandler<AiMessage> {

        private static final int ESTIMATED_TOTAL = 500;

        private final SseEmitter emitter;
        private final StringBuilder accumulated = new StringBuilder();
        private final AtomicInteger charCount = new AtomicInteger(0);

        StreamingChatHandler(SseEmitter emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onNext(String token) {
            accumulated.append(token);
            int currentCount = charCount.addAndGet(token.length());
            int progress = Math.min((int) ((currentCount * 100.0) / ESTIMATED_TOTAL), 95);

            SseHelper.sendEvent(emitter, "token", Map.of(
                    "token", token,
                    "accumulated", accumulated.toString(),
                    "progress", progress,
                    "charCount", currentCount
            ));
        }

        @Override
        public void onComplete(Response<AiMessage> response) {
            int totalChars = accumulated.length();
            SseHelper.sendEvent(emitter, "done", Map.of(
                    "content", accumulated.toString(),
                    "totalChars", totalChars,
                    "progress", 100
            ));
        }

        @Override
        public void onError(Throwable error) {
            logger.error("流式生成出错: {}", error.getMessage(), error);
            SseHelper.sendError(emitter, "生成失败: " + error.getMessage());
        }
    }

    private static class PostStreamingChatHandler implements dev.langchain4j.model.StreamingResponseHandler<AiMessage> {

        private static final int ESTIMATED_TOTAL = 800;
        private static final Logger logger = LoggerFactory.getLogger(PostStreamingChatHandler.class);

        private final SseEmitter emitter;
        private final StringBuilder accumulated = new StringBuilder();
        private final AtomicInteger charCount = new AtomicInteger(0);

        PostStreamingChatHandler(SseEmitter emitter) {
            this.emitter = emitter;
        }

        @Override
        public void onNext(String token) {
            accumulated.append(token);
            int currentCount = charCount.addAndGet(token.length());
            int progress = Math.min((int) ((currentCount * 100.0) / ESTIMATED_TOTAL), 95);

            SseHelper.sendEvent(emitter, "token", Map.of(
                    "token", token,
                    "accumulated", accumulated.toString(),
                    "progress", progress,
                    "charCount", currentCount
            ));
        }

        @Override
        public void onComplete(Response<AiMessage> response) {
            String fullContent = accumulated.toString();
            int totalChars = fullContent.length();

            // 解析帖子内容
            String title = extractSection(fullContent, "【标题】", "【正文】");
            String body = extractSection(fullContent, "【正文】", "【标签】");
            String tags = extractSection(fullContent, "【标签】", null);

            // 如果解析失败，使用简单分割
            if (title.isEmpty()) {
                String[] lines = fullContent.split("\n", 2);
                title = lines[0].trim();
                body = lines.length > 1 ? lines[1].trim() : fullContent;
            }

            SseHelper.sendEvent(emitter, "done", Map.of(
                    "title", title,
                    "content", body.isEmpty() ? fullContent : body,
                    "tags", tags,
                    "totalChars", totalChars,
                    "progress", 100
            ));
        }

        @Override
        public void onError(Throwable error) {
            logger.error("流式生成出错: {}", error.getMessage(), error);
            SseHelper.sendError(emitter, "生成失败: " + error.getMessage());
        }

        private String extractSection(String content, String startMarker, String endMarker) {
            int start = content.indexOf(startMarker);
            if (start == -1) return "";
            start += startMarker.length();

            int end;
            if (endMarker != null) {
                end = content.indexOf(endMarker, start);
                if (end == -1) end = content.length();
            } else {
                end = content.length();
            }

            return content.substring(start, end).trim();
        }
    }
}
