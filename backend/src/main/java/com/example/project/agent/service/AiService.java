package com.example.project.agent.service;

import com.example.project.agent.dto.*;
import com.example.project.agent.flow.dto.FlowResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AiService {

    ChatResponse chat(ChatRequest request);

    CompletableFuture<ChatResponse> chatAsync(ChatRequest request);

    PostGenerateResponse generatePost(PostGenerateRequest request);

    PostGenerateResponse generateAndPublishPost(PostGenerateRequest request);

    FlowResult executeFlow(String flowName, Map<String, Object> input);

    Map<String, Object> getExecutionHistory(String executionId);
}
