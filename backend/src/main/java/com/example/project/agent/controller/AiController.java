package com.example.project.agent.controller;

import com.example.project.agent.dto.*;
import com.example.project.agent.service.AiService;
import com.example.project.agent.flow.FlowContext;
import com.example.project.agent.flow.dto.FlowResult;
import com.example.project.common.Result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统一 AI 控制器
 *
 * ============================================================
 *                    API 接口说明
 * ============================================================
 *
 * 【AI 对话】
 * POST /api/ai/chat
 * 请求：{ "message": "你好", "mode": "SIMPLE" }
 * 响应：{ "reply": "你好！", "executionId": "xxx", "executionTime": 1234 }
 *
 * 【AI 对话 - ReAct 模式】
 * POST /api/ai/chat
 * 请求：{ "message": "最近有什么热门话题？", "mode": "REACT" }
 *
 * 【生成帖子】
 * POST /api/ai/post/generate
 * 请求：{ "topic": "周末爬山", "category": "户外活动" }
 * 响应：{ "title": "...", "content": "...", "executionId": "xxx" }
 *
 * 【生成并发布帖子】
 * POST /api/ai/post/publish
 * 请求：{ "topic": "周末爬山", "category": "户外活动", "publish": true }
 * 响应：{ "title": "...", "content": "...", "published": true, "postId": 123 }
 *
 * 【执行自定义流程】
 * POST /api/ai/flow/{flowName}
 * flowName: post-generation, react-qa
 *
 * 【查询执行历史】
 * GET /api/ai/history/{executionId}
 *
 * ============================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * AI 对话
     *
     * @param request 对话请求
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("AI chat request: mode={}", request.getMode());
        ChatResponse response = aiService.chat(request);
        return Result.success(response);
    }

    /**
     * 生成帖子
     *
     * @param request 生成请求
     */
    @PostMapping("/post/generate")
    public Result<PostGenerateResponse> generatePost(@RequestBody PostGenerateRequest request) {
        log.info("Generate post request: topic={}", request.getTopic());
        PostGenerateResponse response = aiService.generatePost(request);
        return Result.success(response);
    }

    /**
     * 生成并发布帖子
     *
     * @param request 生成请求（publish=true 时会自动发布）
     */
    @PostMapping("/post/publish")
    public Result<PostGenerateResponse> generateAndPublishPost(@RequestBody PostGenerateRequest request) {
        log.info("Generate and publish post request: topic={}", request.getTopic());
        PostGenerateResponse response = aiService.generateAndPublishPost(request);
        return Result.success(response);
    }

    /**
     * 执行自定义流程
     *
     * @param flowName 流程名称：post-generation, react-qa
     * @param input 输入参数
     */
    @PostMapping("/flow/{flowName}")
    public Result<FlowResult> executeFlow(
            @PathVariable String flowName,
            @RequestBody Map<String, Object> input) {
        log.info("Execute flow request: flowName={}", flowName);
        FlowResult result = aiService.executeFlow(flowName, input);
        if (result.isSuccess()) {
            return Result.success(result);
        } else {
            return Result.error(500, "流程执行失败: " + result.getError());
        }
    }

    /**
     * 查询执行历史
     *
     * @param executionId 执行 ID
     */
    @GetMapping("/history/{executionId}")
    public Result<FlowContext> getExecutionHistory(@PathVariable String executionId) {
        log.info("Get execution history: executionId={}", executionId);
        FlowContext context = aiService.getExecutionHistory(executionId);
        if (context == null) {
            return Result.error(404, "执行记录不存在");
        }
        return Result.success(context);
    }
}
