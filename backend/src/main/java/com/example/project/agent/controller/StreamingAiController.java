package com.example.project.agent.controller;

import com.example.project.agent.dto.PostGenerateRequest;
import com.example.project.agent.flow.sse.SseHelper;
import com.example.project.agent.service.StreamingAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@RestController
@RequestMapping("/api/ai/stream")
public class StreamingAiController {

    private static final Logger logger = LoggerFactory.getLogger(StreamingAiController.class);
    private static final int MAX_CONCURRENT_CONNECTIONS = 50;

    private final Semaphore connectionSemaphore = new Semaphore(MAX_CONCURRENT_CONNECTIONS);
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Autowired
    private StreamingAiService streamingAiService;

    @PostMapping(value = "/post/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGeneratePost(@RequestBody PostGenerateRequest request) {
        if (!connectionSemaphore.tryAcquire()) {
            SseEmitter rejected = new SseEmitter();
            SseHelper.sendError(rejected, "服务器繁忙，请稍后重试");
            return rejected;
        }

        String requestId = java.util.UUID.randomUUID().toString();
        logger.info("Stream generate post request: requestId={}, topic={}", requestId, request.getTopic());

        SseEmitter emitter = new SseEmitter(300000L);
        emitters.put(requestId, emitter);

        emitter.onCompletion(() -> releaseConnection(requestId));
        emitter.onTimeout(() -> releaseConnection(requestId));
        emitter.onError((e) -> releaseConnection(requestId));

        streamingAiService.streamGeneratePost(request, requestId, emitter);

        return emitter;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, String> request) {
        if (!connectionSemaphore.tryAcquire()) {
            SseEmitter rejected = new SseEmitter();
            SseHelper.sendError(rejected, "服务器繁忙，请稍后重试");
            return rejected;
        }

        String requestId = java.util.UUID.randomUUID().toString();
        String message = request.get("message");
        logger.info("Stream chat request: requestId={}, message={}", requestId,
                message != null ? message.substring(0, Math.min(50, message.length())) : "null");

        SseEmitter emitter = new SseEmitter(120000L);
        emitters.put(requestId, emitter);

        emitter.onCompletion(() -> releaseConnection(requestId));
        emitter.onTimeout(() -> releaseConnection(requestId));
        emitter.onError((e) -> releaseConnection(requestId));

        streamingAiService.streamChat(message, requestId, emitter);

        return emitter;
    }

    private void releaseConnection(String requestId) {
        emitters.remove(requestId);
        connectionSemaphore.release();
    }
}
