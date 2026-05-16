package com.example.project.agent.service.impl;

import com.example.project.agent.dto.*;
import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.agent.flow.dispatch.FlowDispatchService;
import com.example.project.agent.service.AiService;
import com.example.project.entity.Post;
import com.example.project.service.PostService;
import com.example.project.shiro.util.UserContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final FlowDispatchService flowDispatchService;
    private final PostService postService;

    @Override
    public ChatResponse chat(ChatRequest request) {
        String conversationId = request.getConversationId();
        String message = request.getMessage();

        log.info("AI chat: mode={}, conversationId={}, message={}",
                request.getMode(),
                conversationId,
                message.substring(0, Math.min(50, message.length())));

        try {
            FlowResult result = flowDispatchService.dispatchChat(message, conversationId, request.getMode());

            String reply = result.getOutputAs();
            return ChatResponse.builder()
                    .reply(reply != null ? reply : "抱歉，处理您的消息时出现错误。")
                    .conversationId(conversationId)
                    .executionId(result.getExecutionId())
                    .executionTime(result.getExecutionTimeMs())
                    .nodesExecuted(result.getExecutedNodeCount())
                    .build();

        } catch (Exception e) {
            log.error("Chat error", e);
            return ChatResponse.builder()
                    .reply("抱歉，处理您的消息时出现错误，请稍后重试。")
                    .conversationId(conversationId)
                    .build();
        }
    }

    @Override
    public CompletableFuture<ChatResponse> chatAsync(ChatRequest request) {
        return CompletableFuture.supplyAsync(() -> chat(request));
    }

    @Override
    public PostGenerateResponse generatePost(PostGenerateRequest request) {
        log.info("Generating post: topic={}, category={}", request.getTopic(), request.getCategory());

        Map<String, Object> input = new HashMap<>();
        input.put("topic", request.getTopic());
        input.put("category", request.getCategory());
        input.put("style", request.getStyle());
        input.put("requirements", request.getRequirements());
        input.put("userId", UserContext.getUserId());
        input.put("conversationId", request.getConversationId());

        FlowResult result = flowDispatchService.dispatch("post-generation", input);

        String outputStr = result.getOutputAs() != null ? result.getOutputAs().toString() : null;
        PostContent content = PostContentParser.parse(outputStr);

        return PostGenerateResponse.builder()
                .title(content.getTitle())
                .content(content.getContent())
                .category(request.getCategory())
                .executionId(result.getExecutionId())
                .executionTime(result.getExecutionTimeMs())
                .nodesExecuted(result.getExecutedNodeCount())
                .published(false)
                .build();
    }

    @Override
    public PostGenerateResponse agentGeneratePost(PostGenerateRequest request) {
        log.info("Agent generating post: topic={}, category={}, conversationId={}",
                request.getTopic(), request.getCategory(), request.getConversationId());

        Map<String, Object> input = new HashMap<>();
        input.put("topic", request.getTopic());
        input.put("category", request.getCategory());
        input.put("style", request.getStyle());
        input.put("requirements", request.getRequirements());
        input.put("userId", UserContext.getUserId());
        input.put("conversationId", request.getConversationId());

        FlowResult result = flowDispatchService.dispatch("post-agent", input);

        String outputStr = result.getOutputAs() != null ? result.getOutputAs().toString() : null;
        log.info("Agent result: success={}, outputLength={}, output={}",
                result.isSuccess(),
                outputStr != null ? outputStr.length() : 0,
                outputStr != null ? outputStr.substring(0, Math.min(200, outputStr.length())) : "null");

        PostContent content = PostContentParser.parse(outputStr);

        return PostGenerateResponse.builder()
                .title(content.getTitle())
                .content(content.getContent())
                .category(request.getCategory())
                .executionId(result.getExecutionId())
                .executionTime(result.getExecutionTimeMs())
                .nodesExecuted(result.getExecutedNodeCount())
                .published(false)
                .build();
    }

    @Override
    public PostGenerateResponse generateAndPublishPost(PostGenerateRequest request) {
        PostGenerateResponse response = generatePost(request);

        if (response.getTitle() == null || response.getContent() == null) {
            return response;
        }

        try {
            Post post = new Post();
            post.setTitle(response.getTitle());
            post.setContent(response.getContent());
            post.setCategory(request.getCategory());
            post.setUserId(UserContext.getUserId());

            boolean created = postService.createPost(post);

            if (created && post.getId() != null) {
                response.setPublished(true);
                response.setPostId(post.getId());
                log.info("Post published: postId={}", post.getId());
            }
        } catch (Exception e) {
            log.error("Failed to publish post", e);
        }

        return response;
    }

    @Override
    public FlowResult executeFlow(String flowName, Map<String, Object> input) {
        log.info("Executing custom flow: flowName={}", flowName);
        return flowDispatchService.dispatch(flowName, input);
    }

    @Override
    public Map<String, Object> getExecutionHistory(String executionId) {
        FlowContext context = flowDispatchService.getExecutionHistory(executionId);
        if (context == null) {
            return null;
        }
        return toSafeExecutionSummary(context);
    }

    private Map<String, Object> toSafeExecutionSummary(FlowContext context) {
        Map<String, Object> safeResult = new HashMap<>();
        safeResult.put("executionId", context.getExecutionId());
        safeResult.put("flowId", context.getFlowId());
        safeResult.put("status", context.getStatus());
        safeResult.put("executionTimeMs", context.getExecutionTimeMs());
        safeResult.put("nodeCount", context.getExecutionHistory().size());
        safeResult.put("steps", context.getExecutionHistory().stream()
                .map(step -> Map.of(
                        "nodeId", step.getNodeId(),
                        "nodeName", step.getNodeName(),
                        "success", String.valueOf(step.isSuccess()),
                        "timestamp", step.getTimestamp().toString()
                ))
                .toList());
        return safeResult;
    }
}
