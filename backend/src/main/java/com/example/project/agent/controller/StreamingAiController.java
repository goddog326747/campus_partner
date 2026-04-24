package com.example.project.agent.controller;

import com.alibaba.fastjson.JSON;
import com.example.project.agent.dto.PostGenerateRequest;
import com.example.project.agent.service.StreamingAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流式 AI 控制器
 * <p>
 * 提供 Server-Sent Events (SSE) 流式接口，实现AI生成内容的实时推送
 * </p>
 *
 * @author system
 * @since 1.0
 */
@RestController
@RequestMapping("/api/ai/stream")
public class StreamingAiController {

    private static final Logger logger = LoggerFactory.getLogger(StreamingAiController.class);

    @Autowired
    private StreamingAiService streamingAiService;

    // 存储活跃的 emitters
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 流式生成帖子
     * <p>
     * 使用 SSE 实时推送生成进度和结果
     * </p>
     *
     * @param request 帖子生成请求
     * @return SseEmitter 流式响应
     */
    @PostMapping(value = "/post/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGeneratePost(@RequestBody PostGenerateRequest request) {
        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("Stream generate post request: requestId={}, topic={}", requestId, request.getTopic());

        // 创建 SSE emitter，超时设置为5分钟
        SseEmitter emitter = new SseEmitter(300000L);
        emitters.put(requestId, emitter);

        // 清理 emitter
        emitter.onCompletion(() -> {
            logger.info("SSE completed: requestId={}", requestId);
            emitters.remove(requestId);
        });
        emitter.onTimeout(() -> {
            logger.warn("SSE timeout: requestId={}", requestId);
            emitters.remove(requestId);
        });
        emitter.onError((e) -> {
            logger.error("SSE error: requestId={}", requestId, e);
            emitters.remove(requestId);
        });

        // 异步执行流式生成
        streamingAiService.streamGeneratePost(request, requestId, emitter);

        return emitter;
    }

    /**
     * 流式 AI 对话
     * <p>
     * 使用 SSE 实时推送 AI 回复
     * </p>
     *
     * @param request 对话请求
     * @return SseEmitter 流式响应
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, String> request) {
        String requestId = java.util.UUID.randomUUID().toString();
        String message = request.get("message");
        logger.info("Stream chat request: requestId={}, message={}", requestId,
                message != null ? message.substring(0, Math.min(50, message.length())) : "null");

        SseEmitter emitter = new SseEmitter(120000L);
        emitters.put(requestId, emitter);

        emitter.onCompletion(() -> emitters.remove(requestId));
        emitter.onTimeout(() -> emitters.remove(requestId));
        emitter.onError((e) -> emitters.remove(requestId));

        streamingAiService.streamChat(message, requestId, emitter);

        return emitter;
    }

    /**
     * 发送 SSE 事件
     *
     * @param emitter   SSE emitter
     * @param eventType 事件类型
     * @param data      数据
     */
    public static void sendEvent(SseEmitter emitter, String eventType, Object data) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name(eventType)
                    .data(JSON.toJSONString(data));
            emitter.send(event);
        } catch (IOException e) {
            logger.error("Failed to send SSE event: type={}", eventType, e);
        }
    }

    /**
     * 发送 SSE 完成事件
     *
     * @param emitter SSE emitter
     */
    public static void sendComplete(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("complete").data("{}"));
            emitter.complete();
        } catch (IOException e) {
            logger.error("Failed to send complete event", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 发送 SSE 错误事件
     *
     * @param emitter SSE emitter
     * @param error   错误信息
     */
    public static void sendError(SseEmitter emitter, String error) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(JSON.toJSONString(Map.of("error", error))));
            emitter.complete();
        } catch (IOException e) {
            logger.error("Failed to send error event", e);
            emitter.completeWithError(e);
        }
    }
}
