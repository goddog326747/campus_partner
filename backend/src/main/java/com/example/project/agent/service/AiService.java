package com.example.project.agent.service;

import com.example.project.agent.dto.*;
import com.example.project.agent.flow.dto.FlowResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 统一 AI 服务接口
 * 
 * ============================================================
 *                    统一 AI 服务说明
 * ============================================================
 * 
 * 这个服务整合了所有的 AI 能力：
 * 
 * 1. 简单对话 - 基础 AI 对话功能
 * 2. ReAct 问答 - 支持工具调用的智能问答
 * 3. 帖子生成 - 基于 Agent Flow 的帖子生成
 * 4. 流程执行 - 执行自定义的 Agent Flow
 * 
 * 底层统一基于 Agent Flow 框架实现
 * 
 * ============================================================
 */
public interface AiService {
    
    /**
     * AI 对话
     * 
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponse chat(ChatRequest request);
    
    /**
     * AI 对话（异步）
     */
    CompletableFuture<ChatResponse> chatAsync(ChatRequest request);
    
    /**
     * 生成帖子
     * 
     * @param request 生成请求
     * @return 生成的帖子内容
     */
    PostGenerateResponse generatePost(PostGenerateRequest request);
    
    /**
     * 生成并发布帖子
     * 
     * @param request 生成请求
     * @return 生成并发布的帖子信息
     */
    PostGenerateResponse generateAndPublishPost(PostGenerateRequest request);
    
    /**
     * 执行自定义流程
     * 
     * @param flowName 流程名称
     * @param input 输入参数
     * @return 执行结果
     */
    FlowResult executeFlow(String flowName, Map<String, Object> input);
    
    /**
     * 获取执行历史
     */
    com.example.project.agent.flow.FlowContext getExecutionHistory(String executionId);
}
