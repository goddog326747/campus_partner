package com.example.project.agent.flow.sse;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * SSE 辅助工具类
 * <p>
 * 提供 Server-Sent Events (SSE) 的便捷发送方法，
 * 包括发送事件、发送完成信号和发送错误信息。
 * 所有方法均为静态方法，可直接调用。
 * </p>
 *
 * @author system
 * @since 1.0
 */
public class SseHelper {

    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(SseHelper.class);

    /**
     * 发送 SSE 事件
     *
     * @param emitter   SSE 发射器
     * @param eventType 事件类型
     * @param data      事件数据，会被序列化为 JSON
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
     * 发送完成事件并关闭 SSE 连接
     *
     * @param emitter SSE 发射器
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
     * 发送错误事件并关闭 SSE 连接
     *
     * @param emitter SSE 发射器
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
