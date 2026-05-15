package com.example.project.agent.controller;

import com.example.project.agent.dto.*;
import com.example.project.agent.service.AiService;
import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.common.Result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("AI chat request: mode={}", request.getMode());
        ChatResponse response = aiService.chat(request);
        return Result.success(response);
    }

    @PostMapping("/post/generate")
    public Result<PostGenerateResponse> generatePost(@RequestBody PostGenerateRequest request) {
        log.info("Generate post request: topic={}", request.getTopic());
        PostGenerateResponse response = aiService.generatePost(request);
        return Result.success(response);
    }

    @PostMapping("/post/publish")
    public Result<PostGenerateResponse> generateAndPublishPost(@RequestBody PostGenerateRequest request) {
        log.info("Generate and publish post request: topic={}", request.getTopic());
        PostGenerateResponse response = aiService.generateAndPublishPost(request);
        return Result.success(response);
    }

    @PostMapping("/flow/{flowName}")
    public Result<Map<String, Object>> executeFlow(
            @PathVariable String flowName,
            @RequestBody Map<String, Object> input) {
        log.info("Execute flow request: flowName={}", flowName);
        FlowResult result = aiService.executeFlow(flowName, input);
        if (result.isSuccess()) {
            Map<String, Object> safeResult = Map.of(
                    "flowId", result.getFlowId(),
                    "executionId", result.getExecutionId(),
                    "success", true,
                    "output", result.getOutput() != null ? result.getOutput() : "",
                    "executionTimeMs", result.getExecutionTimeMs(),
                    "nodesExecuted", result.getExecutedNodeCount()
            );
            return Result.success(safeResult);
        } else {
            return Result.error(500, "流程执行失败: " + result.getError());
        }
    }

    @GetMapping("/history/{executionId}")
    public Result<Map<String, Object>> getExecutionHistory(@PathVariable String executionId) {
        log.info("Get execution history: executionId={}", executionId);
        Map<String, Object> history = aiService.getExecutionHistory(executionId);
        if (history == null) {
            return Result.error(404, "执行记录不存在");
        }
        return Result.success(history);
    }
}
