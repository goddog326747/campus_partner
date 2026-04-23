package com.example.project.service;

import com.example.project.agent.flow.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Agent Flow 服务接口
 * 
 * 提供流程编排和执行的核心能力
 */
public interface AgentFlowService {
    
    /**
     * 创建一个新的流程构建器
     */
    AgentFlow.Builder createFlow(String name);
    
    /**
     * 创建一个新的流程构建器（指定模式）
     */
    AgentFlow.Builder createFlow(String name, FlowMode mode);
    
    /**
     * 执行流程
     */
    FlowResult executeFlow(AgentFlow flow, Map<String, Object> input);
    
    /**
     * 异步执行流程
     */
    CompletableFuture<FlowResult> executeFlowAsync(AgentFlow flow, Map<String, Object> input);
    
    /**
     * 获取流程执行历史
     */
    FlowContext getExecutionHistory(String executionId);
    
    /**
     * 创建规划执行模式的帖子生成流程
     * 
     * 流程：
     * 1. 分析用户需求
     * 2. 查询用户风格（工具）
     * 3. 获取热门话题（工具）
     * 4. 生成帖子内容（LLM）
     * 5. 检查敏感词（工具）
     * 6. 返回结果
     */
    AgentFlow createPostGenerationFlow();
    
    /**
     * 创建 ReAct 模式的智能问答流程
     * 
     * 流程：
     * - Thought: 分析问题
     * - Action: 选择工具
     * - Observation: 观察结果
     * - [循环直到找到答案]
     * - Answer: 返回答案
     */
    AgentFlow createReActQaFlow();
}
