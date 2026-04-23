package com.example.project.controller;

import com.example.project.agent.flow.*;
import com.example.project.common.Result;
import com.example.project.service.AgentFlowService;
import com.example.project.util.UserContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent Flow 控制器
 * 
 * ============================================================
 *                    API 接口说明
 * ============================================================
 * 
 * 【规划执行模式】帖子生成
 * POST /api/agent-flow/post/generate
 * 
 * 请求体：
 * {
 *   "topic": "周末爬山",
 *   "style": "活泼",
 *   "requirements": "吸引大学生参加"
 * }
 * 
 * 响应：
 * {
 *   "code": 200,
 *   "data": {
 *     "title": "周末爬山约起来！",
 *     "content": "...",
 *     "executionId": "xxx",
 *     "executionTime": 3500
 *   }
 * }
 * 
 * 【ReAct 模式】智能问答
 * POST /api/agent-flow/qa
 * 
 * 请求体：
 * {
 *   "question": "最近有什么热门的户外活动？"
 * }
 * 
 * 响应：
 * {
 *   "code": 200,
 *   "data": {
 *     "answer": "根据搜索结果，最近热门的户外活动有...",
 *     "executionId": "xxx",
 *     "iterations": 3
 *   }
 * }
 * 
 * 【执行历史查询】
 * GET /api/agent-flow/history/{executionId}
 * 
 * ============================================================
 */
@RestController
@RequestMapping("/api/agent-flow")
public class AgentFlowController {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentFlowController.class);
    
    private final AgentFlowService agentFlowService;
    
    @Autowired
    public AgentFlowController(AgentFlowService agentFlowService) {
        this.agentFlowService = agentFlowService;
    }
    
    /**
     * 规划执行模式 - 生成帖子
     * 
     * 使用 Planning 模式，按预定义流程生成帖子
     */
    @PostMapping("/post/generate")
    public Result<PostGenerateResponse> generatePost(@RequestBody PostGenerateRequest request) {
        logger.info("Generating post with topic: {}", request.getTopic());
        
        try {
            // 获取当前用户ID
            Long userId = UserContext.get() != null ? UserContext.get().getId() : null;
            
            // 创建帖子生成流程
            AgentFlow flow = agentFlowService.createPostGenerationFlow();
            
            // 准备输入
            Map<String, Object> input = new HashMap<>();
            input.put("topic", request.getTopic());
            input.put("style", request.getStyle());
            input.put("requirements", request.getRequirements());
            input.put("userId", userId);
            
            // 执行流程
            FlowResult result = agentFlowService.executeFlow(flow, input);
            
            if (!result.isSuccess()) {
                logger.error("Post generation failed: {}", result.getError());
                return Result.error(500, "帖子生成失败: " + result.getError());
            }
            
            // 解析生成的内容
            String generatedContent = result.getOutputAs();
            PostContent content = parseGeneratedContent(generatedContent);
            
            PostGenerateResponse response = new PostGenerateResponse();
            response.setTitle(content.getTitle());
            response.setContent(content.getContent());
            response.setExecutionId(result.getExecutionId());
            response.setExecutionTime(result.getExecutionTimeMs());
            response.setNodesExecuted(result.getExecutedNodeCount());
            
            logger.info("Post generated successfully: executionId={}, time={}ms", 
                    result.getExecutionId(), result.getExecutionTimeMs());
            
            return Result.success(response);
            
        } catch (Exception e) {
            logger.error("Post generation error", e);
            return Result.error(500, "帖子生成异常: " + e.getMessage());
        }
    }
    
    /**
     * ReAct 模式 - 智能问答
     * 
     * 使用 ReAct 模式，边推理边执行
     */
    @PostMapping("/qa")
    public Result<QaResponse> reactQa(@RequestBody QaRequest request) {
        logger.info("ReAct QA with question: {}", request.getQuestion());
        
        try {
            // 获取当前用户ID
            Long userId = UserContext.get() != null ? UserContext.get().getId() : null;
            
            // 创建 ReAct 问答流程
            AgentFlow flow = agentFlowService.createReActQaFlow();
            
            // 准备输入
            Map<String, Object> input = new HashMap<>();
            input.put("question", request.getQuestion());
            input.put("userId", userId);
            
            // 执行流程
            FlowResult result = agentFlowService.executeFlow(flow, input);
            
            if (!result.isSuccess()) {
                logger.error("ReAct QA failed: {}", result.getError());
                return Result.error(500, "问答失败: " + result.getError());
            }
            
            QaResponse response = new QaResponse();
            response.setAnswer(result.getOutputAs());
            response.setExecutionId(result.getExecutionId());
            response.setExecutionTime(result.getExecutionTimeMs());
            response.setNodesExecuted(result.getExecutedNodeCount());
            
            logger.info("ReAct QA completed: executionId={}, time={}ms, nodes={}", 
                    result.getExecutionId(), result.getExecutionTimeMs(), result.getExecutedNodeCount());
            
            return Result.success(response);
            
        } catch (Exception e) {
            logger.error("ReAct QA error", e);
            return Result.error(500, "问答异常: " + e.getMessage());
        }
    }
    
    /**
     * 查询执行历史
     */
    @GetMapping("/history/{executionId}")
    public Result<ExecutionHistoryResponse> getExecutionHistory(@PathVariable String executionId) {
        logger.info("Querying execution history: {}", executionId);
        
        FlowContext context = agentFlowService.getExecutionHistory(executionId);
        
        if (context == null) {
            return Result.error(404, "执行记录不存在");
        }
        
        ExecutionHistoryResponse response = new ExecutionHistoryResponse();
        response.setExecutionId(executionId);
        response.setFlowId(context.getFlowId());
        response.setStatus(context.getStatus().name());
        response.setStartTime(context.getStartTime().toString());
        response.setEndTime(context.getEndTime() != null ? context.getEndTime().toString() : null);
        response.setExecutionTimeMs(context.getExecutionTimeMs());
        
        // 构建执行步骤
        response.setSteps(context.getExecutionHistory().stream()
                .map(step -> {
                    ExecutionStepResponse stepResponse = new ExecutionStepResponse();
                    stepResponse.setNodeId(step.getNodeId());
                    stepResponse.setNodeName(step.getNodeName());
                    stepResponse.setSuccess(step.isSuccess());
                    stepResponse.setTimestamp(step.getTimestamp().toString());
                    
                    FlowNodeExecutionResult result = context.getNodeResults().get(step.getNodeId());
                    if (result != null) {
                        stepResponse.setOutput(result.getOutput() != null ? 
                                result.getOutput().toString().substring(0, 
                                        Math.min(200, result.getOutput().toString().length())) : null);
                        stepResponse.setExecutionTimeMs(result.getExecutionTimeMs());
                    }
                    
                    return stepResponse;
                })
                .toList());
        
        return Result.success(response);
    }
    
    /**
     * 解析生成的帖子内容
     */
    private PostContent parseGeneratedContent(String content) {
        PostContent result = new PostContent();
        
        // 尝试提取标题（假设标题在第一行或以"标题："开头）
        String[] lines = content.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.startsWith("标题：") || firstLine.startsWith("标题:")) {
                result.setTitle(firstLine.substring(3).trim());
                result.setContent(content.substring(firstLine.length()).trim());
            } else if (firstLine.length() < 30) {
                // 假设短的第一行是标题
                result.setTitle(firstLine);
                result.setContent(content.substring(firstLine.length()).trim());
            } else {
                result.setTitle("生成的帖子");
                result.setContent(content);
            }
        } else {
            result.setTitle("生成的帖子");
            result.setContent(content);
        }
        
        return result;
    }
    
    // ============ 请求/响应类 ============
    
    public static class PostGenerateRequest {
        private String topic;
        private String style;
        private String requirements;
        
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getStyle() { return style; }
        public void setStyle(String style) { this.style = style; }
        public String getRequirements() { return requirements; }
        public void setRequirements(String requirements) { this.requirements = requirements; }
    }
    
    public static class PostGenerateResponse {
        private String title;
        private String content;
        private String executionId;
        private long executionTime;
        private int nodesExecuted;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public int getNodesExecuted() { return nodesExecuted; }
        public void setNodesExecuted(int nodesExecuted) { this.nodesExecuted = nodesExecuted; }
    }
    
    public static class QaRequest {
        private String question;
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }
    
    public static class QaResponse {
        private String answer;
        private String executionId;
        private long executionTime;
        private int nodesExecuted;
        
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public int getNodesExecuted() { return nodesExecuted; }
        public void setNodesExecuted(int nodesExecuted) { this.nodesExecuted = nodesExecuted; }
    }
    
    public static class ExecutionHistoryResponse {
        private String executionId;
        private String flowId;
        private String status;
        private String startTime;
        private String endTime;
        private long executionTimeMs;
        private java.util.List<ExecutionStepResponse> steps;
        
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
        public java.util.List<ExecutionStepResponse> getSteps() { return steps; }
        public void setSteps(java.util.List<ExecutionStepResponse> steps) { this.steps = steps; }
    }
    
    public static class ExecutionStepResponse {
        private String nodeId;
        private String nodeName;
        private boolean success;
        private String output;
        private long executionTimeMs;
        private String timestamp;
        
        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }
        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    private static class PostContent {
        private String title;
        private String content;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
